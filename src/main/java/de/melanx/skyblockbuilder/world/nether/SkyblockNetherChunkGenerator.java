package de.melanx.skyblockbuilder.world.nether;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.world.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.StructureManager;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class SkyblockNetherChunkGenerator extends ChunkGenerator {
    // [VanillaCopy] overworld chunk generator codec
    protected static final Codec<SkyblockNetherChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    BiomeProvider.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeProvider),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    DimensionSettings.field_236098_b_.fieldOf("settings").forGetter((gen) -> gen.settings)
            ).apply(instance, instance.stable(SkyblockNetherChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<DimensionSettings> settings;

    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, new ResourceLocation(SkyblockBuilder.MODID, "skyblock_nether"), CODEC);
    }

    public SkyblockNetherChunkGenerator(BiomeProvider provider, long seed, Supplier<DimensionSettings> settings) {
        super(provider, provider, settings.get().getStructures(), seed);
        this.seed = seed;
        this.settings = settings;
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    @Nonnull
    @Override
    public ChunkGenerator func_230349_a_(long newSeed) {
        return new SkyblockOverworldChunkGenerator(this.biomeProvider.getBiomeProvider(newSeed), newSeed, this.settings);
    }

    @Override
    public void generateSurface(@Nonnull WorldGenRegion region, @Nonnull IChunk chunk) {

    }

    @Override
    public void func_230352_b_(@Nonnull IWorld world, @Nonnull StructureManager manager, @Nonnull IChunk chunk) {

    }

    @Override
    public int getHeight(int x, int z, @Nonnull Heightmap.Type heightmapType) {
        return 0;
    }

    @Override
    public void func_230350_a_(long seed, @Nonnull BiomeManager manager, @Nonnull IChunk chunk, @Nonnull GenerationStage.Carving carving) {

    }

    @Override
    public void func_230351_a_(@Nonnull WorldGenRegion region, @Nonnull StructureManager manager) {
        // here could be your structure
    }

    @Nonnull
    @Override
    public IBlockReader func_230348_a_(int posX, int posY) {
        return new Blockreader(new BlockState[0]);
    }
}
