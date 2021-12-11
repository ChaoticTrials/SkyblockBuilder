package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class SkyblockNetherChunkGenerator extends NoiseBasedChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockNetherChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    RegistryLookupCodec.create(Registry.NOISE_REGISTRY).forGetter(gen -> gen.noises),
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((gen) -> gen.generatorSettings)
            ).apply(instance, instance.stable(SkyblockNetherChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<NoiseGeneratorSettings> generatorSettings;
    protected final StructureSettings settings;
    protected final List<FlatLayerInfo> layerInfos;
    private final int layerHeight;

    public SkyblockNetherChunkGenerator(Registry<NormalNoise.NoiseParameters> noises, BiomeSource provider, long seed, Supplier<NoiseGeneratorSettings> generatorSettings) {
        super(noises, provider, seed, generatorSettings);
        this.seed = seed;
        this.generatorSettings = generatorSettings;
        this.settings = RandomUtility.modifiedStructureSettings(generatorSettings.get().structureSettings());
        this.layerInfos = ConfigHandler.World.surface
                ? WorldUtil.layersInfoFromString(ConfigHandler.World.surfaceSettings.get(Level.NETHER.location().toString()))
                : Lists.newArrayList();
        this.layerHeight = WorldUtil.calculateHeightFromLayers(this.layerInfos);
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
        return new SkyblockNetherChunkGenerator(this.noises, this.biomeSource.withSeed(seed), seed, this.generatorSettings);
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk) {
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
    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull Blender blender, @Nonnull StructureFeatureManager manager, @Nonnull ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Nullable
    @Override
    public BlockPos findNearestMapFeature(@Nonnull ServerLevel level, StructureFeature<?> structure, @Nonnull BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        boolean shouldSearch = WorldUtil.isStructureGenerated(structure.getRegistryName());
        return shouldSearch ? super.findNearestMapFeature(level, structure, pos, searchRadius, skipKnownStructures) : null;
    }

    @Override
    public int getBaseHeight(int x, int z, @Nonnull Heightmap.Types heightmapType, @Nonnull LevelHeightAccessor level) {
        if (ConfigHandler.World.surface) {
            return this.layerHeight;
        }

        return 0;
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull BiomeManager biomeManager, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {

    }

    // Vanilla copy
//    @Override
//    protected void createStructure(@Nonnull ConfiguredStructureFeature<?, ?> feature, @Nonnull RegistryAccess registry, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull StructureManager templateManager, long seed, @Nonnull Biome biome) {
//        SectionPos sectionpos = SectionPos.bottomOf(chunk);
//        StructureStart<?> existingStart = structureManager.getStartForFeature(sectionpos, feature.feature, chunk);
//        int references = existingStart != null ? existingStart.getReferences() : 0;
//        StructureFeatureConfiguration structurefeatureconfiguration = this.settings.getConfig(feature.feature);
//        if (structurefeatureconfiguration != null) {
//            StructureStart<?> start = feature.generate(registry, this, this.biomeSource, templateManager, seed, chunk.getPos(), biome, references, structurefeatureconfiguration, chunk);
//            structureManager.setStartForFeature(sectionpos, feature.feature, start, chunk);
//        }
//    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level) {
        return new NoiseColumn(level.getMinBuildHeight(), new BlockState[0]);
    }
}
