package de.melanx.skyblockbuilder.util;

import com.google.common.collect.ImmutableList;
import de.melanx.skyblockbuilder.compat.CuriosCompat;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.fml.ModList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class RandomUtility {

    public static RegistryAccess dynamicRegistries = null;

    public static Biome modifyCopyBiome(Biome biome) {
        Biome newBiome = new Biome(biome.climateSettings, biome.getBiomeCategory(), biome.getDepth(), biome.getScale(), biome.getSpecialEffects(), modifyCopyGeneration(biome.getGenerationSettings()), biome.getMobSettings());
        if (biome.getRegistryName() != null) {
            newBiome.setRegistryName(biome.getRegistryName());
        }

        return newBiome;
    }

    public static BiomeGenerationSettings modifyCopyGeneration(BiomeGenerationSettings settings) {
        // Remove non-whitelisted structures
        ImmutableList.Builder<Supplier<ConfiguredStructureFeature<?, ?>>> structures = ImmutableList.builder();

        for (Supplier<ConfiguredStructureFeature<?, ?>> structure : settings.structures()) {
            ResourceLocation location = structure.get().feature.getRegistryName();
            if (location != null) {
                if (ConfigHandler.Structures.generationStructures.test(location)) {
                    structures.add(structure);
                }
            }
        }

        // Remove non-whitelisted features
        ImmutableList.Builder<List<Supplier<ConfiguredFeature<?, ?>>>> featureList = ImmutableList.builder();

        settings.features().forEach(list -> {
            ImmutableList.Builder<Supplier<ConfiguredFeature<?, ?>>> features = ImmutableList.builder();
            for (Supplier<ConfiguredFeature<?, ?>> feature : list) {
                ResourceLocation location = feature.get().feature.getRegistryName();
                if (location != null) {
                    if (ConfigHandler.Structures.generationFeatures.test(location)) {
                        features.add(feature);
                    }
                }
            }
            featureList.add(features.build());
        });

        return new BiomeGenerationSettings(settings.getSurfaceBuilder(), settings.carvers, featureList.build(), structures.build());
    }

    public static int validateBiome(Biome biome) {
        if (dynamicRegistries != null) {
            Registry<Biome> lookup = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
            return lookup.getId(lookup.get(biome.getRegistryName()));
        } else {
            return -1;
        }
    }

    public static void dropInventories(Player player) {
        if (player.isSpectator() || player.isCreative()) {
            return;
        }

        player.getInventory().dropAll();
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.dropInventory(player);
        }
    }

    public static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("\\W+", "_");
    }

    public static String getFilePath(String folderPath, String name) {
        return getFilePath(folderPath, name, "nbt");
    }

    public static String getFilePath(String folderPath, String name, String extension) {
        int index = 0;
        String filename;
        String filepath;
        do {
            filename = (name == null ? "template" : RandomUtility.normalize(name)) + ((index == 0) ? "" : "_" + index) + "." + extension;
            index++;
            filepath = folderPath + "/" + filename;
        } while (Files.exists(Paths.get(filepath)));

        return filepath;
    }
}
