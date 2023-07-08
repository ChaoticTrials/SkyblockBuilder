package de.melanx.skyblockbuilder.world.presets;

import de.melanx.skyblockbuilder.config.common.DimensionsConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.util.BiomeSourceConverter;
import de.melanx.skyblockbuilder.util.WorldPresetUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.List;
import java.util.Map;

public class SkyblockPreset extends WorldPreset {

    public SkyblockPreset(RegistryAccess registryAccess) {
        super(SkyblockPreset.dimensions(registryAccess));
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> dimensions(RegistryAccess registryAccess) {
        HolderGetter<DimensionType> dimensionTypes = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);

        HolderGetter<MultiNoiseBiomeSourceParameterList> noises = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = registryAccess.lookupOrThrow(Registries.BIOME);

        return Map.of(
                LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        configuredOverworldChunkGenerator(registryAccess)),
                LevelStem.NETHER, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                        DimensionsConfig.Nether.Default ?
                                WorldPresetUtil.defaultNetherGenerator(registryAccess)
                                : netherChunkGenerator(noises, noiseGeneratorSettings)),
                LevelStem.END, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                        DimensionsConfig.End.Default ?
                                WorldPresetUtil.defaultEndGenerator(registryAccess)
                                : endChunkGenerator(biomes, noiseGeneratorSettings))
        );
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(RegistryAccess registryAccess) {
        HolderGetter<MultiNoiseBiomeSourceParameterList> noises = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);

        return DimensionsConfig.Overworld.Default ?
                new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)), noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD))
                : overworldChunkGenerator(noises, noiseGeneratorSettings);
    }

    public static ChunkGenerator overworldChunkGenerator(HolderGetter<MultiNoiseBiomeSourceParameterList> noises, HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings) {
        MultiNoiseBiomeSource biomeSource = (MultiNoiseBiomeSource) BiomeSourceConverter.customBiomeSource(Level.OVERWORLD, MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        return new SkyblockNoiseBasedChunkGenerator(biomeSource, settings, Level.OVERWORLD, SkyblockPreset.getLayers(Level.OVERWORLD));
    }

    private static ChunkGenerator netherChunkGenerator(HolderGetter<MultiNoiseBiomeSourceParameterList> noises, HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings) {
        BiomeSource biomeSource = BiomeSourceConverter.customBiomeSource(Level.NETHER, MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);

        return new SkyblockNoiseBasedChunkGenerator(biomeSource, settings, Level.NETHER, SkyblockPreset.getLayers(Level.NETHER));
    }

    private static ChunkGenerator endChunkGenerator(HolderGetter<Biome> biomes, HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings) {
        TheEndBiomeSource biomeSource = (TheEndBiomeSource) BiomeSourceConverter.customBiomeSource(Level.END, TheEndBiomeSource.create(biomes));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(biomeSource, settings, Level.END, SkyblockPreset.getLayers(Level.END));
    }

    public static List<FlatLayerInfo> getLayers(ResourceKey<Level> levelKey) {
        return WorldConfig.surface
                ? WorldUtil.layersInfoFromString(WorldConfig.surfaceSettings.get(levelKey.location().toString()))
                : List.of();
    }
}
