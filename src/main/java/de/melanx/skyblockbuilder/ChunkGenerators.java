package de.melanx.skyblockbuilder;

import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "CHUNK_GENERATOR")
public class ChunkGenerators {

    public static final Codec<SkyblockNoiseBasedChunkGenerator> noiseBased = SkyblockNoiseBasedChunkGenerator.CODEC;
    public static final Codec<SkyblockEndChunkGenerator> theEnd = SkyblockEndChunkGenerator.CODEC;
}
