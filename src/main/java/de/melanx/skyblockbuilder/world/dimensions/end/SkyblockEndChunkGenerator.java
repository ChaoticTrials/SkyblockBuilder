package de.melanx.skyblockbuilder.world.dimensions.end;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class SkyblockEndChunkGenerator extends ChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockEndChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((gen) -> gen.settings)
            ).apply(instance, instance.stable(SkyblockEndChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    protected final NoiseBasedChunkGenerator parent;
    protected final List<FlatLayerInfo> layerInfos;

    public SkyblockEndChunkGenerator(BiomeSource provider, long seed, Supplier<NoiseGeneratorSettings> settings) {
        super(provider, provider, settings.get().structureSettings(), seed);
        this.seed = seed;
        this.settings = settings;
        this.parent = new NoiseBasedChunkGenerator(provider, seed, settings);
        this.layerInfos = ConfigHandler.World.surface
                ? WorldUtil.layersInfoFromString(ConfigHandler.World.surfaceSettings.get(Level.END.location().toString()))
                : Lists.newArrayList();
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getSeaLevel() {
        return ConfigHandler.World.seaHeight;
    }

    @Nonnull
    @Override
    public ChunkGenerator withSeed(long seed) {
        return new SkyblockEndChunkGenerator(this.biomeSource.withSeed(seed), seed, this.settings);
    }

    @Override
    public void buildSurfaceAndBedrock(@Nonnull WorldGenRegion level, @Nonnull ChunkAccess chunk) {
        if (ConfigHandler.World.surface) {
            ChunkPos cp = chunk.getPos();
            int xs = cp.getMinBlockX();
            int zs = cp.getMinBlockZ();
            int xe = cp.getMaxBlockX();
            int ze = cp.getMaxBlockZ();
            int y = 0;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (FlatLayerInfo info : this.layerInfos) {
                BlockState state = info.getBlockState();
                for (int i = 0; i < info.getHeight(); i++) {
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

        if (ConfigHandler.Dimensions.End.mainIsland) {
            this.parent.buildSurfaceAndBedrock(level, chunk);
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 0 && chunkPos.z == 0) {
            chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.defaultBlockState(), false);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull StructureFeatureManager manager, @Nonnull ChunkAccess chunk) {
        if (ConfigHandler.Dimensions.End.mainIsland) {
            return this.parent.fillFromNoise(executor, manager, chunk);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Nullable
    @Override
    public BlockPos findNearestMapFeature(@Nonnull ServerLevel level, StructureFeature<?> structure, @Nonnull BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        boolean shouldSearch = RandomUtility.isStructureGenerated(structure.getRegistryName());
        return shouldSearch ? super.findNearestMapFeature(level, structure, pos, searchRadius, skipKnownStructures) : null;
    }

    @Override
    public int getBaseHeight(int x, int z, @Nonnull Heightmap.Types heightmapType, @Nonnull LevelHeightAccessor level) {
        if (ConfigHandler.World.surface) {
            int i = 0;
            for (FlatLayerInfo info : this.layerInfos) {
                i += info.getHeight();
            }
            return i;
        }

        return this.parent.getBaseHeight(x, z, heightmapType, level);
    }

    @Override
    public void applyCarvers(long seed, @Nonnull BiomeManager biomeManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {
        ChunkPos pos = chunk.getPos();
        int value = 10 * 16;
        if (pos.getMinBlockX() < value && pos.getMinBlockX() > -value && pos.getMinBlockZ() < value && pos.getMinBlockZ() > -value) {
            super.applyCarvers(seed, biomeManager, chunk, carving);
        }
    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level) {
        return new NoiseColumn(0, new BlockState[0]);
    }
}
