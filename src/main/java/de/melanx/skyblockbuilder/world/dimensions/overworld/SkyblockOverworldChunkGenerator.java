package de.melanx.skyblockbuilder.world.dimensions.overworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SkyblockOverworldChunkGenerator extends ChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockOverworldChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    BiomeProvider.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeProvider),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    DimensionSettings.DIMENSION_SETTINGS_CODEC.fieldOf("settings").forGetter((gen) -> gen.settings)
            ).apply(instance, instance.stable(SkyblockOverworldChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<DimensionSettings> settings;
    protected final NoiseChunkGenerator parent;
    protected final List<FlatLayerInfo> layerInfos;

    public SkyblockOverworldChunkGenerator(BiomeProvider provider, long seed, Supplier<DimensionSettings> settings) {
        super(provider, provider, settings.get().getStructures(), seed);
        this.seed = seed;
        this.settings = settings;
        this.parent = new NoiseChunkGenerator(provider, seed, settings);
        this.layerInfos = LibXConfigHandler.World.surface ? WorldUtil.layersInfoFromString(LibXConfigHandler.World.surfaceSettings) : new ArrayList<>();
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
        return new SkyblockOverworldChunkGenerator(this.biomeProvider.getBiomeProvider(newSeed), newSeed, this.settings);
    }

    @Override
    public void generateSurface(@Nonnull WorldGenRegion region, @Nonnull IChunk chunk) {
        if (LibXConfigHandler.World.surface) {
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
    public void generateStructures(@Nonnull IWorld world, @Nonnull StructureManager manager, @Nonnull IChunk chunk) {

    }

    @Nullable
    @Override
    public BlockPos findStructure(@Nonnull ServerWorld world, Structure<?> structure, @Nonnull BlockPos startPos, int radius, boolean skipExistingChunks) {
        boolean shouldSearch = RandomUtility.isStructureGenerated(structure.getRegistryName());
        return shouldSearch ? super.findStructure(world, structure, startPos, radius, skipExistingChunks) : null;
    }

    @Override
    public int getHeight(int x, int z, @Nonnull Heightmap.Type heightmapType) {
        if (LibXConfigHandler.World.surface) {
            int i = 0;
            for (FlatLayerInfo info : this.layerInfos) {
                i += info.getLayerCount();
            }
            return i;
        }

        return this.parent.getHeight(x, z, heightmapType);
    }

    @Override
    public void generateCarvings(long seed, @Nonnull BiomeManager manager, @Nonnull IChunk chunk, @Nonnull GenerationStage.Carving carving) {

    }

    // Vanilla copy
    @Override
    public void func_242707_a(@Nonnull DynamicRegistries dynamicRegistries, @Nonnull StructureManager structureManager, @Nonnull IChunk chunk, @Nonnull TemplateManager templateManager, long seed) {
        ChunkPos chunkpos = chunk.getPos();
        Biome biome = this.biomeProvider.getNoiseBiome((chunkpos.x << 2) + 2, 0, (chunkpos.z << 2) + 2);
        if (RandomUtility.isStructureGenerated(Structure.STRONGHOLD.getRegistryName())) {
            this.func_242705_a(StructureFeatures.STRONGHOLD, dynamicRegistries, structureManager, chunk, templateManager, seed, chunkpos, biome);
        }

        for (Supplier<StructureFeature<?, ?>> supplier : biome.getGenerationSettings().getStructures()) {
            this.func_242705_a(supplier.get(), dynamicRegistries, structureManager, chunk, templateManager, seed, chunkpos, biome);
        }
    }

    @Nonnull
    @Override
    public IBlockReader func_230348_a_(int posX, int posY) {
        return new Blockreader(new BlockState[0]);
    }
}
