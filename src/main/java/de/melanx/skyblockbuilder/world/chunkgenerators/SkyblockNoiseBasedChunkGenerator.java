package de.melanx.skyblockbuilder.world.chunkgenerators;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.WorldUtil;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SkyblockNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> ChunkGenerator.commonCodec(instance)
                    .and(instance.group(
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(generator -> generator.noises),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension)
                    )).apply(instance, instance.stable(SkyblockNoiseBasedChunkGenerator::new)));

    public final long seed;
    public final Registry<NormalNoise.NoiseParameters> noises;
    public final Holder<NoiseGeneratorSettings> generatorSettings;
    public final ResourceKey<Level> dimension;
    protected final NoiseBasedChunkGenerator parent;
    protected final List<FlatLayerInfo> layerInfos;
    private final int layerHeight;

    public SkyblockNoiseBasedChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource source, long seed, Holder<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension) {
        super(structureSets, noises, source, seed, generatorSettings);
        this.seed = seed;
        this.noises = noises;
        this.generatorSettings = generatorSettings;
        this.parent = new NoiseBasedChunkGenerator(structureSets, this.noises, source, seed, generatorSettings);
        this.dimension = dimension;
        this.layerInfos = ConfigHandler.World.surface
                ? WorldUtil.layersInfoFromString(ConfigHandler.World.surfaceSettings.get(dimension.location().toString()))
                : Lists.newArrayList();
        this.layerHeight = WorldUtil.calculateHeightFromLayers(this.layerInfos);
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return SkyblockNoiseBasedChunkGenerator.CODEC;
    }

    @Override
    public int getSeaLevel() {
        return ConfigHandler.World.seaHeight;
    }

    @Nonnull
    @Override
    public ChunkGenerator withSeed(long seed) {
        return new SkyblockNoiseBasedChunkGenerator(this.structureSets, this.noises, this.biomeSource.withSeed(seed), seed, this.generatorSettings, this.dimension);
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk) {
        if (ConfigHandler.World.surface) {
            ChunkPos cp = chunk.getPos();
            int xs = cp.getMinBlockX();
            int zs = cp.getMinBlockZ();
            int xe = cp.getMaxBlockX();
            int ze = cp.getMaxBlockZ();
            int y = level.getMinBuildHeight();
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
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(@Nonnull ServerLevel level, @Nonnull HolderSet<ConfiguredStructureFeature<?, ?>> structureSet, @Nonnull BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        for (Holder<ConfiguredStructureFeature<?, ?>> holder : structureSet) {
            if (!ConfigHandler.Structures.generationStructures.test(holder.value().feature.getRegistryName())) {
                return null;
            }
        }

        return super.findNearestMapFeature(level, structureSet, pos, searchRadius, skipKnownStructures);
    }

    @Override
    public int getBaseHeight(int x, int z, @Nonnull Heightmap.Types heightmapType, @Nonnull LevelHeightAccessor level) {
        if (ConfigHandler.World.surface) {
            return this.layerHeight;
        }

        return this.parent.getBaseHeight(x, z, heightmapType, level);
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull BiomeManager biomeManager, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {

    }

    @Override
    protected boolean tryGenerateStructure(@Nonnull StructureSet.StructureSelectionEntry structureEntry, @Nonnull StructureFeatureManager structureManager, @Nonnull RegistryAccess registry, @Nonnull StructureManager featureManager, long seed, @Nonnull ChunkAccess chunk, @Nonnull ChunkPos chunkPos, @Nonnull SectionPos sectionPos) {
        if (!ConfigHandler.Structures.generationStructures.test(structureEntry.structure().value().feature.getRegistryName())) {
            return false;
        }

        return super.tryGenerateStructure(structureEntry, structureManager, registry, featureManager, seed, chunk, chunkPos, sectionPos);
    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    // [Vanilla copy] to ignore some features
    @Override
    public void applyBiomeDecoration(@Nonnull WorldGenLevel level, @Nonnull ChunkAccess chunk, @Nonnull StructureFeatureManager structureFeatureManager) {
        ChunkPos chunkPos = chunk.getPos();
        SectionPos sectionPos = SectionPos.of(chunkPos, level.getMinSection());
        BlockPos blockPos = sectionPos.origin();

        Registry<ConfiguredStructureFeature<?, ?>> configuredStructureFeatureRegistry = level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        Map<Integer, List<ConfiguredStructureFeature<?, ?>>> featuresMap = configuredStructureFeatureRegistry.stream().collect(Collectors.groupingBy((feature) -> feature.feature.step().ordinal()));
        List<BiomeSource.StepFeatureData> stepFeatureDataList = this.biomeSource.featuresPerStep();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
        long decorationSeed = worldgenRandom.setDecorationSeed(level.getSeed(), blockPos.getX(), blockPos.getZ());

        Set<Biome> possibleBiomes = new ObjectArraySet<>();
        ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach((pos) -> {
            ChunkAccess chunkAccess = level.getChunk(pos.x, pos.z);

            for (LevelChunkSection chunkSection : chunkAccess.getSections()) {
                chunkSection.getBiomes().getAll((biomeHolder) -> {
                    possibleBiomes.add(biomeHolder.value());
                });
            }

        });
        possibleBiomes.retainAll(this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));

        int dataSize = stepFeatureDataList.size();

        try {
            Registry<PlacedFeature> placedFeatureRegistry = level.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            int maxDecorations = Math.max(GenerationStep.Decoration.values().length, dataSize);

            for (int i = 0; i < maxDecorations; ++i) {
                int index = 0;
                if (structureFeatureManager.shouldGenerateFeatures()) {
                    for (ConfiguredStructureFeature<?, ?> feature : featuresMap.getOrDefault(i, Collections.emptyList())) {
                        worldgenRandom.setFeatureSeed(decorationSeed, index, i);
                        Supplier<String> supplier = () -> configuredStructureFeatureRegistry.getResourceKey(feature).map(Object::toString).orElseGet(feature::toString);

                        try {
                            level.setCurrentlyGenerating(supplier);
                            structureFeatureManager.startsForFeature(sectionPos, feature).forEach((structureStart) -> {
                                structureStart.placeInChunk(level, structureFeatureManager, this, worldgenRandom, ChunkGenerator.getWritableArea(chunk), chunkPos);
                            });
                        } catch (Exception exception) {
                            CrashReport report = CrashReport.forThrowable(exception, "Feature placement");
                            report.addCategory("Feature").setDetail("Description", supplier::get);
                            throw new ReportedException(report);
                        }

                        ++index;
                    }
                }

                if (i < dataSize) {
                    IntSet mapping = new IntArraySet();

                    for (Biome biome : possibleBiomes) {
                        List<HolderSet<PlacedFeature>> holderSets = biome.getGenerationSettings().features();
                        if (i < holderSets.size()) {
                            HolderSet<PlacedFeature> featureHolderSet = holderSets.get(i);
                            BiomeSource.StepFeatureData stepFeatureData = stepFeatureDataList.get(i);
                            //noinspection CodeBlock2Expr
                            featureHolderSet.stream().map(Holder::value).forEach((feature) -> {
                                mapping.add(stepFeatureData.indexMapping().applyAsInt(feature));
                            });
                        }
                    }

                    int mappingSize = mapping.size();
                    int[] array = mapping.toIntArray();
                    Arrays.sort(array);
                    BiomeSource.StepFeatureData stepFeatureData = stepFeatureDataList.get(i);

                    for (int j = 0; j < mappingSize; ++j) {
                        int featureIndex = array[j];
                        PlacedFeature placedfeature = stepFeatureData.features().get(featureIndex);
                        // The only reason why I needed to copy the code - checking if it should be placed
                        if (!ConfigHandler.Structures.generationFeatures.test(placedfeature.feature().value().feature().getRegistryName())) {
                            continue;
                        }

                        Supplier<String> currentlyGenerating = () -> placedFeatureRegistry.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
                        worldgenRandom.setFeatureSeed(decorationSeed, featureIndex, i);

                        try {
                            level.setCurrentlyGenerating(currentlyGenerating);
                            placedfeature.placeWithBiomeCheck(level, this, worldgenRandom, blockPos);
                        } catch (Exception exception1) {
                            CrashReport report = CrashReport.forThrowable(exception1, "Feature placement");
                            report.addCategory("Feature").setDetail("Description", currentlyGenerating::get);
                            throw new ReportedException(report);
                        }
                    }
                }
            }

            level.setCurrentlyGenerating(null);
        } catch (Exception exception2) {
            CrashReport report = CrashReport.forThrowable(exception2, "Biome decoration");
            report.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Seed", decorationSeed);
            throw new ReportedException(report);
        }
    }
}
