package de.melanx.skyblockbuilder.world.presets;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.BiomeSourceConverter;
import de.melanx.skyblockbuilder.util.WorldPresetUtil;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.Map;

public class SkyblockPreset extends WorldPreset {

    public SkyblockPreset() {
        super(SkyblockPreset.dimensions());
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> dimensions() {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
        Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
        Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;

        Registry<DimensionType> dimensionTypes = BuiltinRegistries.DIMENSION_TYPE;
        //noinspection deprecation
        Registry<Biome> biomes = BuiltinRegistries.BIOME;

        return Map.of(
                LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        configuredOverworldChunkGenerator()),
                LevelStem.NETHER, new LevelStem(dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.NETHER),
                        ConfigHandler.Dimensions.Nether.Default ?
                                WorldPresetUtil.defaultNetherGenerator()
                                : netherChunkGenerator(structureSets, noises, biomes, noiseGeneratorSettings)),
                LevelStem.END, new LevelStem(dimensionTypes.getOrCreateHolderOrThrow(BuiltinDimensionTypes.END),
                        ConfigHandler.Dimensions.End.Default ?
                                WorldPresetUtil.defaultEndGenerator()
                                : endChunkGenerator(structureSets, noises, biomes, noiseGeneratorSettings))
        );
    }

    public static ChunkGenerator configuredOverworldChunkGenerator() {
        Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
        Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;
        //noinspection deprecation
        Registry<Biome> biomes = BuiltinRegistries.BIOME;
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;

        return ConfigHandler.Dimensions.Overworld.Default ?
                new NoiseBasedChunkGenerator(structureSets, BuiltinRegistries.NOISE, MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes, true), noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD).getOrThrow(false, System.out::println))
                : overworldChunkGenerator(structureSets, noises, biomes, noiseGeneratorSettings);
    }

    public static ChunkGenerator overworldChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry) {
        MultiNoiseBiomeSource biomeSource = (MultiNoiseBiomeSource) BiomeSourceConverter.customBiomeSource(Level.OVERWORLD, MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry, false));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);

        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, biomeSource, settings, Level.OVERWORLD);
    }

    private static ChunkGenerator netherChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry) {
        BiomeSource biomeSource = BiomeSourceConverter.customBiomeSource(Level.NETHER, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolderOrThrow(NoiseGeneratorSettings.NETHER);

        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, biomeSource, settings, Level.NETHER);
    }

    private static ChunkGenerator endChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry) {
        TheEndBiomeSource biomeSource = (TheEndBiomeSource) BiomeSourceConverter.customBiomeSource(Level.END, new TheEndBiomeSource(biomeRegistry));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolderOrThrow(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(structureSets, noises, biomeSource, settings, Level.END);
    }
}
