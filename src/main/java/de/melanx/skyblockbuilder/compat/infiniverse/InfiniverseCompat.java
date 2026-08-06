package de.melanx.skyblockbuilder.compat.infiniverse;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.commoble.infiniverse.api.InfiniverseAPI;
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
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

public class InfiniverseCompat {

    public static final String MODID = "infiniverse";

    public static void markDimensionForUnregistration(final MinecraftServer server, final ResourceKey<Level> levelToRemove) {
        InfiniverseAPI.get().markDimensionForUnregistration(server, levelToRemove);
    }

    public static ServerLevel getOrCreateLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, RegistryAccess registryAccess) {
        LevelStem spawnDimension = InfiniverseCompat.getSpawnDimension(registryAccess);

        if (spawnDimension == null) {
            SkyblockBuilder.getLogger().warn("Configured spawn dimension {} does not exist, using the overworld", SpawnConfig.spawnDimension.identifier());
            return InfiniverseCompat.getOrCreateOverworldLevel(server, levelKey, registryAccess);
        }

        if (Level.OVERWORLD.equals(SpawnConfig.spawnDimension)) {
            return InfiniverseCompat.getOrCreateOverworldLevel(server, levelKey, registryAccess);
        }

        if (Level.NETHER.equals(SpawnConfig.spawnDimension)) {
            return InfiniverseCompat.getOrCreateNetherLevel(server, levelKey, registryAccess);
        }

        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> {
            return new LevelStem(spawnDimension.type(), spawnDimension.generator());
        });
    }

    public static ServerLevel getOrCreateOverworldLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, RegistryAccess registryAccess) {
        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> {
            Registry<DimensionType> dimensionTypes = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);
            Registry<MultiNoiseBiomeSourceParameterList> noises = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            Registry<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
            Registry<Biome> biomes = registryAccess.lookupOrThrow(Registries.BIOME);

            return new LevelStem(
                    dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                    SkyblockPreset.configuredOverworldChunkGenerator(
                            noises,
                            biomes,
                            noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                    )
            );
        });
    }

    public static ServerLevel getOrCreateNetherLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, RegistryAccess registryAccess) {
        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> {
            Registry<DimensionType> dimensionTypes = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);
            Registry<MultiNoiseBiomeSourceParameterList> noises = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            Registry<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
            Registry<Biome> biomes = registryAccess.lookupOrThrow(Registries.BIOME);

            return new LevelStem(
                    dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                    SkyblockPreset.netherChunkGenerator(noises, noiseGeneratorSettings, biomes)
            );
        });
    }

    @Nullable
    private static LevelStem getSpawnDimension(RegistryAccess registryAccess) {
        // Shares its id with Registries.DIMENSION, Infiniverse registers the dimensions it creates here as well
        Registry<LevelStem> levelStems = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);

        return levelStems.getValue(Registries.levelToLevelStem(SpawnConfig.spawnDimension));
    }

    public static boolean useInfiniverse() {
        if (!WorldConfig.DimensionPerTeam.enabled) {
            return false;
        }

        if (!ModList.get().isLoaded(MODID)) {
            SkyblockBuilder.getLogger().warn("Infiniverse needs to be installed for dimensions per team! Falling back to shared dimensions.");
            return false;
        }

        return true;
    }
}
