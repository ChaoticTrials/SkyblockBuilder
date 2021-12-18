package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockMultiNoiseBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockNoiseBasedChunkGenerator;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.world.ForgeWorldPreset;

@RegisterClass
public class Registration {

    public static final ForgeWorldPreset customSkyblock = new VoidWorldType();

    public static final Item structureSaver = new ItemStructureSaver();

    public static void registerCodecs() {
        // biome sources
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "multi_noise"), SkyblockMultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_end"), SkyblockEndBiomeSource.CODEC);

        // chunk generators
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "noise_based"), SkyblockNoiseBasedChunkGenerator.CODEC);
    }
}
