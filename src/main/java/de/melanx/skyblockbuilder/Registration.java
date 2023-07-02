package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class Registration {

    public static void registerCodecs() {
        // chunk generators
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, SkyblockBuilder.getInstance().resource("noise_based"), SkyblockNoiseBasedChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, SkyblockBuilder.getInstance().resource("the_end"), SkyblockEndChunkGenerator.CODEC);
    }
}
