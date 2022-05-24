package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.world.SkylandsType;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;

@RegisterClass
public class Registration {

    public static final ForgeWorldPreset skyblock = new VoidWorldType();
    public static final ForgeWorldPreset skylands = new SkylandsType();

    public static final Item structureSaver = new ItemStructureSaver();

    public static final ResourceKey<NoiseGeneratorSettings> SKYLANDS_SETTINGS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation(SkyblockBuilder.getInstance().modid, "test"));

    public static void registerCodecs() {
        // chunk generators
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "noise_based"), SkyblockNoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(SkyblockBuilder.getInstance().modid, "the_end"), SkyblockEndChunkGenerator.CODEC);
        NoiseGeneratorSettings.register(SKYLANDS_SETTINGS, new NoiseGeneratorSettings(SkylandsType.NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SkylandsType.NOISE_ROUTER, SurfaceRuleData.overworldLike(false, false, false), -64, false, false, false, true));
    }
}
