package de.melanx.skyblockbuilder.world.presets;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.DimensionsConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.util.BiomeSourceConverter;
import de.melanx.skyblockbuilder.util.WorldPresetUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.SkyBiomeSource;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SkyblockPreset extends WorldPreset {

    public static final ResourceKey<WorldPreset> KEY = ResourceKey.create(Registries.WORLD_PRESET, SkyblockBuilder.getInstance().resource("skyblock"));

    // Must be lazy as we can't access the full registry while deserializing.
    private final LazyValue<WorldPreset> actualPreset;
    private final HolderLookup.RegistryLookup<Biome> biomes;

    public SkyblockPreset(
            HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup.RegistryLookup<Biome> biomes
    ) {
        // Dummy map
        super(Map.of());
        this.actualPreset = new LazyValue<>(() -> new WorldPreset(dimensions(
                dimensionTypes, noises, noiseGeneratorSettings, biomes
        )));
        this.biomes = biomes;
    }

    public HolderLookup.RegistryLookup<Biome> getBiomes() {
        return this.biomes;
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
            HolderLookup<Biome> biomes
    ) {
        return Map.of(
                LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        configuredOverworldChunkGenerator(noises, noiseGeneratorSettings, biomes)),
                LevelStem.NETHER, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                        DimensionsConfig.Nether.Default ?
                                WorldPresetUtil.defaultNetherGenerator(noises, noiseGeneratorSettings)
                                : netherChunkGenerator(noises, noiseGeneratorSettings, biomes)),
                LevelStem.END, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                        DimensionsConfig.End.Default ?
                                WorldPresetUtil.defaultEndGenerator(noiseGeneratorSettings, biomes)
                                : endChunkGenerator(noiseGeneratorSettings, biomes))
        );
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup<Biome> biomes
    ) {
        return DimensionsConfig.Overworld.Default ?
                new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)), noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD))
                : overworldChunkGenerator(noises, noiseGeneratorSettings, biomes);
    }

    public static ChunkGenerator overworldChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup<Biome> biomes
    ) {
        BiomeSource biomeSource = BiomeSourceConverter.customBiomeSource(Level.OVERWORLD, MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)), biomes);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        biomeSource = SkyblockPreset.convertBiomeSource((MultiNoiseBiomeSource) biomeSource, biomes, DimensionsConfig.Overworld.centeredBiomes);

        return new SkyblockNoiseBasedChunkGenerator(biomeSource, settings, Level.OVERWORLD, SkyblockPreset.getLayers(Level.OVERWORLD));
    }

    private static ChunkGenerator netherChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup<Biome> biomes
    ) {
        BiomeSource biomeSource = BiomeSourceConverter.customBiomeSource(Level.NETHER, MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)), biomes);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);

        biomeSource = SkyblockPreset.convertBiomeSource((MultiNoiseBiomeSource) biomeSource, biomes, DimensionsConfig.Nether.centeredBiomes);

        return new SkyblockNoiseBasedChunkGenerator(biomeSource, settings, Level.NETHER, SkyblockPreset.getLayers(Level.NETHER));
    }

    private static ChunkGenerator endChunkGenerator(
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup<Biome> biomes
    ) {
        TheEndBiomeSource biomeSource = (TheEndBiomeSource) BiomeSourceConverter.customBiomeSource(Level.END, TheEndBiomeSource.create(biomes), biomes);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(biomeSource, settings, Level.END, SkyblockPreset.getLayers(Level.END));
    }

    public static List<FlatLayerInfo> getLayers(ResourceKey<Level> levelKey) {
        return WorldConfig.surface
                ? WorldUtil.layersInfoFromString(WorldConfig.surfaceSettings.get(levelKey.location().toString()))
                : List.of();
    }

    private static BiomeSource convertBiomeSource(MultiNoiseBiomeSource biomeSource, HolderLookup<Biome> biomes, List<DimensionsConfig.UnregisteredCenterBiome> unregisteredCenterBiomes) {
        if (unregisteredCenterBiomes.isEmpty()) {
            return biomeSource;
        }

        List<SkyBiomeSource.CenterBiome> centerBiomes = new ArrayList<>();

        unregisteredCenterBiomes.forEach(biomeConfig -> {
            ResourceKey<Biome> resourceKey = ResourceKey.create(Registries.BIOME, biomeConfig.id());
            Optional<Holder.Reference<Biome>> optionalHolder = biomes.get(resourceKey);
            if (optionalHolder.isEmpty()) {
                SkyblockBuilder.getLogger().error("Could not find biome {} for center biome {}. Use minecraft:plains as fallback.", resourceKey, biomeConfig.id());
                optionalHolder = Optional.of(biomes.getOrThrow(Biomes.PLAINS));
            }
            centerBiomes.add(new SkyBiomeSource.CenterBiome(optionalHolder.get(), biomeConfig.radius()));
        });

        return new SkyBiomeSource(centerBiomes, biomeSource);
    }
}
