package de.melanx.skyblockbuilder.registration;

import com.mojang.serialization.MapCodec;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "CHUNK_GENERATOR")
public class ModChunkGenerators {

    public static final MapCodec<SkyblockNoiseBasedChunkGenerator> noiseBased = SkyblockNoiseBasedChunkGenerator.CODEC;
    public static final MapCodec<SkyblockEndChunkGenerator> theEnd = SkyblockEndChunkGenerator.CODEC;
}
