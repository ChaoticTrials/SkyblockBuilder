package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.world.ForgeWorldType;

@RegisterClass
public class Registration {

    public static final ForgeWorldType customSkyblock = new VoidWorldType();

    public static final Item structureSaver = new ItemStructureSaver();

    public static void registerCodecs() {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "skyblock"), SkyblockOverworldChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "skyblock_provider"), SkyblockBiomeProvider.CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "skyblock_nether"), SkyblockNetherChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "skyblock_nether_provider"), SkyblockNetherBiomeProvider.PACKET_CODEC);

        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "skyblock_end"), SkyblockEndChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(SkyblockBuilder.getInstance().modid, "skyblock_end_provider"), SkyblockEndBiomeProvider.CODEC);
    }
}
