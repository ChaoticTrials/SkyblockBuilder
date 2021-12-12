package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
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
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "overworld"), SkyblockOverworldChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "overworld"), SkyblockOverworldBiomeSource.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_nether"), SkyblockNetherChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_nether"), SkyblockNetherBiomeSource.PACKET_CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_end"), SkyblockEndChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_end"), SkyblockEndBiomeSource.CODEC);
    }
}
