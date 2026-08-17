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
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.fml.ModList;

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

            Holder<DimensionType> dimensionType = InfiniverseCompat.getDimensionType(server, registryAccess, dimensionTypes);

            return new LevelStem(dimensionType, SkyblockPreset.configuredOverworldChunkGenerator(noises.asLookup(), noiseGeneratorSettings.asLookup(), biomes.asLookup()));
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

    private static Holder<DimensionType> getDimensionType(MinecraftServer server, RegistryAccess registryAccess, Registry<DimensionType> dimensionTypes) {
        ResourceKey<Level> spawnDimension = SpawnConfig.spawnDimension;
        if (spawnDimension == Level.OVERWORLD) {
            return dimensionTypes.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
        }

        if (spawnDimension == Level.NETHER) {
            return dimensionTypes.getHolderOrThrow(BuiltinDimensionTypes.NETHER);
        }

        if (spawnDimension == Level.END) {
            return dimensionTypes.getHolderOrThrow(BuiltinDimensionTypes.END);
        }

        ServerLevel spawnLevel = server.getLevel(spawnDimension);
        if (spawnLevel != null) {
            return spawnLevel.dimensionTypeRegistration();
        }

        Registry<LevelStem> levelStems = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
        LevelStem levelStem = levelStems.get(Registries.levelToLevelStem(spawnDimension));

        if (levelStem != null) {
            return levelStem.type();
        }

        SkyblockBuilder.getLogger().warn("Configured spawn dimension {} does not exist, using the overworld dimension type", spawnDimension.location());
        return dimensionTypes.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD);
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
