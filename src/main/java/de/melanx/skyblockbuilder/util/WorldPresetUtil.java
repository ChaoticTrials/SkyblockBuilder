package de.melanx.skyblockbuilder.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class WorldPresetUtil {

    public static ChunkGenerator defaultNetherGenerator(RegistryAccess registryAccess) {
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
        HolderGetter<MultiNoiseBiomeSourceParameterList> multiNoiseBiomeSourceParameterListHolderGetter = registryAccess.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromPreset(multiNoiseBiomeSourceParameterListHolderGetter.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);

        return new NoiseBasedChunkGenerator(biomeSource, settings);
    }

    public static ChunkGenerator defaultEndGenerator(RegistryAccess registryAccess) {
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);
        HolderGetter<Biome> biomes = registryAccess.lookupOrThrow(Registries.BIOME);
        TheEndBiomeSource biomeSource = TheEndBiomeSource.create(biomes);

        return new NoiseBasedChunkGenerator(biomeSource, settings);
    }
}
