package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class SkyblockNetherChunkGenerator extends ChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockNetherChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((gen) -> gen.settings)
            ).apply(instance, instance.stable(SkyblockNetherChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;

    public SkyblockNetherChunkGenerator(BiomeSource provider, long seed, Supplier<NoiseGeneratorSettings> settings) {
        super(provider, provider, settings.get().structureSettings(), seed);
        this.seed = seed;
        this.settings = settings;
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
        return new SkyblockNetherChunkGenerator(this.biomeSource.withSeed(seed), seed, this.settings);
    }

    @Override
    public void buildSurfaceAndBedrock(@Nonnull WorldGenRegion level, @Nonnull ChunkAccess chunk) {

    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull StructureFeatureManager manager, @Nonnull ChunkAccess chunk) {
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
        return 0;
    }

    @Override
    public void applyCarvers(long seed, @Nonnull BiomeManager biomeManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {

    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level) {
        return new NoiseColumn(0, new BlockState[0]);
    }
}
