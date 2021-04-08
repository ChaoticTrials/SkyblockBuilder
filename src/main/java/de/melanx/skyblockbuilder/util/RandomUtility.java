package de.melanx.skyblockbuilder.util;

import com.google.common.collect.ImmutableList;
import de.melanx.skyblockbuilder.ConfigHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class RandomUtility {

    public static final ITextComponent UNKNOWN_PLAYER = new TranslationTextComponent("skyblockbuilder.unknown_player");

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
                if (ConfigHandler.toggleWhitelist.get()) {
                    if (!ConfigHandler.whitelistStructures.get().contains(location.toString())) {
                        structures.add(structure);
                    }
                } else {
                    if (ConfigHandler.whitelistStructures.get().contains(location.toString())) {
                        structures.add(structure);
                    }
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
                    if (ConfigHandler.toggleWhitelist.get()) {
                        if (!ConfigHandler.whitelistFeatures.get().contains(location.toString())) {
                            features.add(feature);
                        }
                    } else {
                        if (ConfigHandler.whitelistFeatures.get().contains(location.toString())) {
                            features.add(feature);
                        }
                    }
                }
            }
            featureList.add(features.build());
        });

        return new BiomeGenerationSettings(settings.getSurfaceBuilder(), settings.carvers, featureList.build(), structures.build());
    }
}
