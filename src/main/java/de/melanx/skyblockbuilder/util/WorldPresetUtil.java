package de.melanx.skyblockbuilder.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class WorldPresetUtil {

    public static ChunkGenerator defaultNetherGenerator(
            HolderGetter<MultiNoiseBiomeSourceParameterList> noises,
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings
    ) {
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromPreset(noises.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
        return new NoiseBasedChunkGenerator(biomeSource, settings);
    }

    public static ChunkGenerator defaultEndGenerator(
            HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings,
            HolderGetter<Biome> biomes
    ) {
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);
        TheEndBiomeSource biomeSource = TheEndBiomeSource.create(biomes);
        return new NoiseBasedChunkGenerator(biomeSource, settings);
    }
}
