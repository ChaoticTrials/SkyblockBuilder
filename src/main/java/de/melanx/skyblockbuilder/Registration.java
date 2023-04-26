package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

public class Registration {

    public static void registerCodecs(RegisterEvent event) {
        // chunk generators
        event.register(Registries.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "noise_based"), () -> SkyblockNoiseBasedChunkGenerator.CODEC);
        event.register(Registries.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_end"), () -> SkyblockEndChunkGenerator.CODEC);
    }
}
