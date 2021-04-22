package de.melanx.skyblockbuilder.world.dimensions.overworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SkyblockOverworldChunkGenerator extends ChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    protected static final Codec<SkyblockOverworldChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    BiomeProvider.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeProvider),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    DimensionSettings.field_236098_b_.fieldOf("settings").forGetter((gen) -> gen.settings)
            ).apply(instance, instance.stable(SkyblockOverworldChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<DimensionSettings> settings;
    protected final NoiseChunkGenerator parent;
    protected final List<FlatLayerInfo> layerInfos;

    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, new ResourceLocation(SkyblockBuilder.MODID, "skyblock"), CODEC);
    }

    public SkyblockOverworldChunkGenerator(BiomeProvider provider, long seed, Supplier<DimensionSettings> settings) {
        super(provider, provider, settings.get().getStructures(), seed);
        this.seed = seed;
        this.settings = settings;
        this.parent = new NoiseChunkGenerator(provider, seed, settings);
        this.layerInfos = ConfigHandler.generateSurface.get() ? WorldUtil.layersInfoFromString(ConfigHandler.generationSettings.get()) : new ArrayList<>();
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    @Override
    public int getSeaLevel() {
        return ConfigHandler.seaHeight.get();
    }

    @Nonnull
    @Override
    public ChunkGenerator func_230349_a_(long newSeed) {
        return new SkyblockOverworldChunkGenerator(this.biomeProvider.getBiomeProvider(newSeed), newSeed, this.settings);
    }

    @Override
    public void generateSurface(@Nonnull WorldGenRegion region, @Nonnull IChunk chunk) {
        if (ConfigHandler.generateSurface.get()) {
            ChunkPos cp = chunk.getPos();
            int xs = cp.getXStart();
            int zs = cp.getZStart();
            int xe = cp.getXEnd();
            int ze = cp.getZEnd();
            int y = 0;
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (FlatLayerInfo info : this.layerInfos) {
                BlockState state = info.getLayerMaterial();
                for (int i = 0; i < info.getLayerCount(); i++) {
                    for (int x = xs; x <= xe; x++) {
                        for (int z = zs; z <= ze; z++) {
                            pos.setX(x);
                            pos.setY(y);
                            pos.setZ(z);
                            chunk.setBlockState(pos, state, false);
                        }
                    }
                    y++;
                }
            }
        }
    }

    @Override
    public void func_230352_b_(@Nonnull IWorld world, @Nonnull StructureManager manager, @Nonnull IChunk chunk) {

    }

    @Nullable
    @Override
    public BlockPos func_235956_a_(@Nonnull ServerWorld world, Structure<?> structure, @Nonnull BlockPos startPos, int radius, boolean skipExististingChunks) {
        boolean shouldSearch = RandomUtility.isStructureGenerated(structure.getRegistryName());
        return shouldSearch ? super.func_235956_a_(world, structure, startPos, radius, skipExististingChunks) : null;
    }

    @Override
    public int getHeight(int x, int z, @Nonnull Heightmap.Type heightmapType) {
        if (ConfigHandler.generateSurface.get()) {
            int i = 0;
            for (FlatLayerInfo info : this.layerInfos) {
                i += info.getLayerCount();
            }
            return i;
        }

        return this.parent.getHeight(x, z, heightmapType);
    }

    @Override
    public void func_230350_a_(long seed, @Nonnull BiomeManager manager, @Nonnull IChunk chunk, @Nonnull GenerationStage.Carving carving) {

    }

//    @Override
//    public void func_230351_a_(@Nonnull WorldGenRegion region, @Nonnull StructureManager manager) {
//        if (ConfigHandler.overworldStructures.get()) {
//            super.func_230351_a_(region, manager);
//        }
//    }

    @Nonnull
    @Override
    public IBlockReader func_230348_a_(int posX, int posY) {
        return new Blockreader(new BlockState[0]);
    }
}
