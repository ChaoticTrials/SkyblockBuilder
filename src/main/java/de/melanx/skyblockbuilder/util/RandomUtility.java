package de.melanx.skyblockbuilder.util;

import com.google.common.collect.ImmutableList;
import de.melanx.skyblockbuilder.compat.CuriosCompat;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraftforge.fml.ModList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

public class RandomUtility {

    public static final ITextComponent UNKNOWN_PLAYER = new TranslationTextComponent("skyblockbuilder.unknown_player");

    public static DynamicRegistries dynamicRegistries = null;

    public static ITextComponent getDisplayNameByUuid(World world, UUID id) {
        PlayerEntity player = world.getPlayerByUuid(id);
        return player != null ? player.getDisplayName() : UNKNOWN_PLAYER;
    }

    public static Biome modifyCopyBiome(Biome biome) {
        Biome newBiome = new Biome(biome.climate, biome.getCategory(), biome.getDepth(), biome.getScale(), biome.getAmbience(), modifyCopyGeneration(biome.getGenerationSettings()), biome.getMobSpawnInfo());
        if (biome.getRegistryName() != null) {
            newBiome.setRegistryName(biome.getRegistryName());
        }
        return newBiome;
    }

    public static BiomeGenerationSettings modifyCopyGeneration(BiomeGenerationSettings settings) {
        // Remove non-whitelisted structures
        ImmutableList.Builder<Supplier<StructureFeature<?, ?>>> structures = ImmutableList.builder();

        for (Supplier<StructureFeature<?, ?>> structure : settings.getStructures()) {
            ResourceLocation location = structure.get().field_236268_b_.getRegistryName();
            if (location != null) {
                if (LibXConfigHandler.Structures.generationStructures.test(location)) {
                    structures.add(structure);
                }
            }
        }

        // Remove non-whitelisted features
        ImmutableList.Builder<List<Supplier<ConfiguredFeature<?, ?>>>> featureList = ImmutableList.builder();

        settings.getFeatures().forEach(list -> {
            ImmutableList.Builder<Supplier<ConfiguredFeature<?, ?>>> features = ImmutableList.builder();
            for (Supplier<ConfiguredFeature<?, ?>> feature : list) {
                ResourceLocation location = feature.get().feature.getRegistryName();
                if (location != null) {
                    if (LibXConfigHandler.Structures.generationFeatures.test(location)) {
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
            Registry<Biome> lookup = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
            return lookup.getId(lookup.getOrDefault(biome.getRegistryName()));
        } else {
            return -1;
        }
    }

    public static void dropInventories(PlayerEntity player) {
        player.inventory.dropAllItems();
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.dropInventory(player);
        }
    }

    public static boolean isStructureGenerated(ResourceLocation registryName) {
        return LibXConfigHandler.Structures.generationStructures.test(registryName) || LibXConfigHandler.Structures.generationFeatures.test(registryName);
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
