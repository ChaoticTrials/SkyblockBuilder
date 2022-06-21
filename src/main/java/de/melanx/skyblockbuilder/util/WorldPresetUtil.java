package de.melanx.skyblockbuilder.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class WorldPresetUtil {

    public static ChunkGenerator defaultNetherGenerator() {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
        Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
        Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;

        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(BuiltinRegistries.BIOME);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.NETHER);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, settings);
    }

    public static ChunkGenerator defaultEndGenerator() {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
        Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
        Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;

        TheEndBiomeSource biomeSource = new TheEndBiomeSource(BuiltinRegistries.BIOME);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolderOrThrow(NoiseGeneratorSettings.END);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, settings);
    }
}
