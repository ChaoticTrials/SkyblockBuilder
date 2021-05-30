package de.melanx.skyblockbuilder.world.dimensions.end;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SkyblockEndChunkGenerator extends ChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockEndChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    BiomeProvider.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeProvider),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    DimensionSettings.DIMENSION_SETTINGS_CODEC.fieldOf("settings").forGetter((gen) -> gen.settings)
            ).apply(instance, instance.stable(SkyblockEndChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<DimensionSettings> settings;
    protected final NoiseChunkGenerator parent;

    public SkyblockEndChunkGenerator(BiomeProvider provider, long seed, Supplier<DimensionSettings> settings) {
        super(provider, provider, settings.get().getStructures(), seed);
        this.seed = seed;
        this.settings = settings;
        this.parent = new NoiseChunkGenerator(provider, seed, settings);
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> getChunkGeneratorCodec() {
        return CODEC;
    }

    @Override
    public int getSeaLevel() {
        return LibXConfigHandler.World.seaHeight;
    }

    @Nonnull
    @Override
    public ChunkGenerator createForSeed(long newSeed) {
        return new SkyblockEndChunkGenerator(this.biomeProvider.getBiomeProvider(newSeed), newSeed, this.settings);
    }

    @Override
    public void generateSurface(@Nonnull WorldGenRegion region, @Nonnull IChunk chunk) {
        if (LibXConfigHandler.Dimensions.End.mainIsland) {
            this.parent.generateSurface(region, chunk);
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 0 && chunkPos.z == 0) {
            chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.getDefaultState(), false);
        }
    }

    @Override
    public void generateStructures(@Nonnull IWorld world, @Nonnull StructureManager manager, @Nonnull IChunk chunk) {
        if (LibXConfigHandler.Dimensions.End.mainIsland) {
            this.parent.generateStructures(world, manager, chunk);
        }
    }

    @Nullable
    @Override
    public BlockPos findStructure(@Nonnull ServerWorld world, Structure<?> structure, @Nonnull BlockPos startPos, int radius, boolean skipExistingChunks) {
        boolean shouldSearch = RandomUtility.isStructureGenerated(structure.getRegistryName());
        return shouldSearch ? super.findStructure(world, structure, startPos, radius, skipExistingChunks) : null;
    }

    @Override
    public int getHeight(int x, int z, @Nonnull Heightmap.Type heightmapType) {
        return this.parent.getHeight(x, z, heightmapType);
    }

    @Override
    public void generateCarvings(long seed, @Nonnull BiomeManager manager, @Nonnull IChunk chunk, @Nonnull GenerationStage.Carving carving) {
        ChunkPos pos = chunk.getPos();
        int value = 10 * 16;
        if (pos.getXStart() < value && pos.getXStart() > -value && pos.getZStart() < value && pos.getZStart() > -value) {
            super.generateCarvings(seed, manager, chunk, carving);
        }
    }

    @Nonnull
    @Override
    public IBlockReader func_230348_a_(int posX, int posY) {
        return new Blockreader(new BlockState[0]);
    }
}
