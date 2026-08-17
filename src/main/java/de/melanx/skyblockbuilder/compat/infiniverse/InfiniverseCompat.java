package de.melanx.skyblockbuilder.compat.infiniverse;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

public class InfiniverseCompat {

    public static final String MODID = "infiniverse";

    public static void markDimensionForUnregistration(final MinecraftServer server, final ResourceKey<Level> levelToRemove) {
        InfiniverseAPI.get().markDimensionForUnregistration(server, levelToRemove);
    }

    public static ServerLevel getOrCreateLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, RegistryAccess registryAccess) {
        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> {
            Registry<DimensionType> dimensionTypes = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);
            Registry<MultiNoiseBiomeSourceParameterList> noises = registryAccess.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            Registry<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.registryOrThrow(Registries.NOISE_SETTINGS);
            Registry<Biome> biomes = registryAccess.registryOrThrow(Registries.BIOME);

            // Team dimensions run on the dimension type of the spawn dimension, so the noise settings must come from there as well
            LevelStem spawnDimension = InfiniverseCompat.getSpawnDimension(registryAccess);
            Holder<DimensionType> dimensionType = InfiniverseCompat.getDimensionType(spawnDimension, dimensionTypes);
            Holder<NoiseGeneratorSettings> settings = InfiniverseCompat.getNoiseGeneratorSettings(spawnDimension, noiseGeneratorSettings);

            return new LevelStem(dimensionType, SkyblockPreset.configuredOverworldChunkGenerator(noises.asLookup(), biomes.asLookup(), settings));
        });
    }

    public static ServerLevel getOrCreateNetherLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, RegistryAccess registryAccess) {
        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> {
            Registry<DimensionType> dimensionTypes = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);
            Registry<MultiNoiseBiomeSourceParameterList> noises = registryAccess.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            Registry<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.registryOrThrow(Registries.NOISE_SETTINGS);
            Registry<Biome> biomes = registryAccess.registryOrThrow(Registries.BIOME);

            return new LevelStem(
                    dimensionTypes.getHolderOrThrow(BuiltinDimensionTypes.NETHER),
                    SkyblockPreset.netherChunkGenerator(noises.asLookup(), noiseGeneratorSettings.asLookup(), biomes.asLookup())
            );
        });
    }

    private static Holder<DimensionType> getDimensionType(@Nullable LevelStem spawnDimension, Registry<DimensionType> dimensionTypes) {
        if (spawnDimension == null) {
            SkyblockBuilder.getLogger().warn("Configured spawn dimension {} does not exist, using the overworld dimension type", SpawnConfig.spawnDimension.location());
            return dimensionTypes.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
        }

        return spawnDimension.type();
    }

    private static Holder<NoiseGeneratorSettings> getNoiseGeneratorSettings(@Nullable LevelStem spawnDimension, Registry<NoiseGeneratorSettings> noiseGeneratorSettings) {
        if (spawnDimension != null && spawnDimension.generator() instanceof NoiseBasedChunkGenerator generator) {
            return generator.generatorSettings();
        }

        return noiseGeneratorSettings.getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
    }

    @Nullable
    private static LevelStem getSpawnDimension(RegistryAccess registryAccess) {
        // Shares its id with Registries.DIMENSION, Infiniverse registers the dimensions it creates here as well
        Registry<LevelStem> levelStems = registryAccess.registryOrThrow(Registries.LEVEL_STEM);

        return levelStems.get(Registries.levelToLevelStem(SpawnConfig.spawnDimension));
    }

    public static boolean useInfiniverse() {
        if (!WorldConfig.dimensionPerTeam) {
            return false;
        }

        if (!ModList.get().isLoaded(MODID)) {
            SkyblockBuilder.getLogger().warn("Infiniverse needs to be installed for dimensions per team! Falling back to shared dimensions.");
            return false;
        }

        return true;
    }
}
