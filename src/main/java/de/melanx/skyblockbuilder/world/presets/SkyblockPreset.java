package de.melanx.skyblockbuilder.world.presets;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.DimensionsConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.datagen.ModBiomeTagProvider;
import de.melanx.skyblockbuilder.datagen.SkyblockBiomeParameters;
import de.melanx.skyblockbuilder.util.BiomeSourceConverter;
import de.melanx.skyblockbuilder.world.SkyBiomeSource;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import de.melanx.skyblockbuilder.world.flat.FlatLayers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
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
        BiomeParametersPreset.init(biomes);
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
                        SkyblockPreset.configuredOverworldChunkGenerator(noises, noiseGeneratorSettings, biomes)),
                LevelStem.NETHER, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                        DimensionsConfig.Nether.isCustom
                                ? SkyblockPreset.netherChunkGenerator(noises, noiseGeneratorSettings, biomes)
                                : SkyblockPreset.defaultNetherGenerator(noises, noiseGeneratorSettings)),
                LevelStem.END, new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                        DimensionsConfig.End.isCustom
                                ? SkyblockPreset.endChunkGenerator(noiseGeneratorSettings, biomes)
                                : SkyblockPreset.defaultEndGenerator(noiseGeneratorSettings, biomes))
        );
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup<Biome> biomes
    ) {
        return DimensionsConfig.Overworld.isCustom
                ? SkyblockPreset.overworldChunkGenerator(noises, noiseGeneratorSettings, biomes)
                : new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)), noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
    }

    public static ChunkGenerator overworldChunkGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderLookup<Biome> biomes
    ) {
        BiomeSource biomeSource = SkyblockPreset.createFilteredBiomeSource(noises.getOrThrow(SkyblockBiomeParameters.KEY), ModBiomeTagProvider.IS_OVERWORLD);
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

    public static FlatLayers getLayers(ResourceKey<Level> levelKey) {
        return WorldConfig.surface
                ? WorldConfig.surfaceSettings.getOrDefault(levelKey.location().toString(), FlatLayers.EMPTY)
                : FlatLayers.EMPTY;
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

    private static ChunkGenerator defaultNetherGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings
    ) {
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
        return new NoiseBasedChunkGenerator(biomeSource, settings);
    }

    private static ChunkGenerator defaultEndGenerator(
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderGetter<Biome> biomes
    ) {
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);
        TheEndBiomeSource biomeSource = TheEndBiomeSource.create(biomes);
        return new NoiseBasedChunkGenerator(biomeSource, settings);
    }

    private static MultiNoiseBiomeSource createFilteredBiomeSource(Holder<MultiNoiseBiomeSourceParameterList> preset, TagKey<Biome> biomeTagKey) {
        return new MultiNoiseBiomeSource(Either.right(preset)) {
            private Climate.ParameterList<Holder<Biome>> modifiedList;

            @Nonnull
            @Override
            public Climate.ParameterList<Holder<Biome>> parameters() {
                return this.parameters.map(parameterList -> parameterList, parameterListHolder -> {
                    if (this.modifiedList == null) {
                        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = parameterListHolder.value().parameters().values()
                                .stream()
                                .filter(pair -> pair.getSecond().is(biomeTagKey))
                                .toList();
                        this.modifiedList = new Climate.ParameterList<>(list);
                    }

                    return this.modifiedList;
                });
            }
        };
    }
}
