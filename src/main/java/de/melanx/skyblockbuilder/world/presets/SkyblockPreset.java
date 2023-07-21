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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SkyblockPreset extends WorldPreset {

    // Must be lazy as we can't access the full registry while seserialising.
    private final LazyValue<WorldPreset> actualPreset;

    public SkyblockPreset(
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderGetter<Biome> biomes
    ) {
        // Dummy map
        super(Map.of());
        this.actualPreset = new LazyValue<>(() -> new WorldPreset(dimensions(
                dimensionTypes, noises, noiseGeneratorSettings, biomes
        )));
    }

    @Nonnull
    @Override
    public WorldDimensions createWorldDimensions() {
        return this.actualPreset.get().createWorldDimensions();
    }

    @Nonnull
    @Override
    public Optional<LevelStem> overworld() {
        return this.actualPreset.get().overworld();
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> dimensions(
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderGetter<Biome> biomes
    ) {
        return Map.of(
                LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        configuredOverworldChunkGenerator(noises, noiseGeneratorSettings)),
                LevelStem.NETHER, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                        DimensionsConfig.Nether.Default ?
                                WorldPresetUtil.defaultNetherGenerator(noises, noiseGeneratorSettings)
                                : netherChunkGenerator(noises, noiseGeneratorSettings)),
                LevelStem.END, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                        DimensionsConfig.End.Default ?
                                WorldPresetUtil.defaultEndGenerator(noiseGeneratorSettings, biomes)
                                : endChunkGenerator(noiseGeneratorSettings, biomes))
        );
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings
    ) {
        return DimensionsConfig.Overworld.Default ?
                new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)), noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD))
                : overworldChunkGenerator(noises, noiseGeneratorSettings);
    }

    public static ChunkGenerator overworldChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings
    ) {
        MultiNoiseBiomeSource biomeSource = (MultiNoiseBiomeSource) BiomeSourceConverter.customBiomeSource(Level.OVERWORLD, MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        return new SkyblockNoiseBasedChunkGenerator(biomeSource, settings, Level.OVERWORLD, SkyblockPreset.getLayers(Level.OVERWORLD));
    }

    private static ChunkGenerator netherChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings
    ) {
        BiomeSource biomeSource = BiomeSourceConverter.customBiomeSource(Level.NETHER, MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);

        return new SkyblockNoiseBasedChunkGenerator(biomeSource, settings, Level.NETHER, SkyblockPreset.getLayers(Level.NETHER));
    }

    private static ChunkGenerator endChunkGenerator(
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderGetter<Biome> biomes
    ) {
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
