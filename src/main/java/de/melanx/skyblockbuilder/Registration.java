package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class Registration {

    public static void registerCodecs() {
        // chunk generators
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "noise_based"), SkyblockNoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_end"), SkyblockEndChunkGenerator.CODEC);
    }
}
