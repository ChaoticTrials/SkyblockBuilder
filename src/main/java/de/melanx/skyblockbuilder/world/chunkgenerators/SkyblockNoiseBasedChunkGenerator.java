package de.melanx.skyblockbuilder.world.chunkgenerators;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
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
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension),
                            FlatLayerInfo.CODEC.listOf().optionalFieldOf("layers").forGetter(generator -> Optional.ofNullable(generator.layerInfos)) // todo 1.20 change to #fieldOf
                    )).apply(instance, instance.stable((structureSets, noises, biomeSource, generatorSettings, dimension, layerInfos) -> {
                        List<FlatLayerInfo> flatLayerInfos = layerInfos.orElseGet(() -> SkyblockPreset.getLayers(dimension));
                        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, biomeSource, generatorSettings, dimension, flatLayerInfos);
                    })));

    public final Registry<NormalNoise.NoiseParameters> noises;
    public final Holder<NoiseGeneratorSettings> generatorSettings;
    public final ResourceKey<Level> dimension;
    protected final NoiseBasedChunkGenerator parent;
    protected final List<FlatLayerInfo> layerInfos;
    private final int layerHeight;

    public SkyblockNoiseBasedChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension, List<FlatLayerInfo> layerInfos) {
        super(structureSets, noises, biomeSource, generatorSettings);
        this.noises = noises;
        this.generatorSettings = generatorSettings;
        this.parent = new NoiseBasedChunkGenerator(structureSets, this.noises, biomeSource, generatorSettings);
        this.dimension = dimension;
        this.layerInfos = layerInfos;
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

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureManager structureManager, @Nonnull RandomState randomState, @Nonnull ChunkAccess chunk) {
        if (!this.layerInfos.isEmpty()) {
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
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull Blender blender, @Nonnull RandomState randomState, @Nonnull StructureManager manager, @Nonnull ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Nullable
    @Override
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(@Nonnull ServerLevel level, @Nonnull HolderSet<Structure> structureHolderSet, @Nonnull BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        List<Holder<Structure>> holders = structureHolderSet.stream().filter(holder -> holder.unwrapKey().isPresent() && ConfigHandler.Structures.generationStructures.test(holder.unwrapKey().get().location())).toList();
        HolderSet.Direct<Structure> modifiedStructureHolderSet = HolderSet.direct(holders);
        for (Holder<Structure> holder : modifiedStructureHolderSet) {
            if (holder.unwrapKey().isPresent() && ConfigHandler.Structures.generationStructures.test(holder.unwrapKey().get().location())) {
                return super.findNearestMapStructure(level, modifiedStructureHolderSet, pos, searchRadius, skipKnownStructures);
            }
        }

        return null;
    }

    @Override
    public int getBaseHeight(int x, int z, @Nonnull Heightmap.Types heightmapType, @Nonnull LevelHeightAccessor level, @Nonnull RandomState randomState) {
        if (ConfigHandler.World.surface) {
            return level.getMinBuildHeight() + this.layerHeight;
        }

        return this.parent.getBaseHeight(x, z, heightmapType, level, randomState);
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull RandomState random, @Nonnull BiomeManager biomeManager, @Nonnull StructureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving step) {
        if (this.layerInfos.isEmpty()) {
            return;
        }

        NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk((otherChunk) -> this.createNoiseChunk(otherChunk, structureManager, Blender.of(level), random));

        ChunkPos chunkPos = chunk.getPos();
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingMask carvingMask = ((ProtoChunk) chunk).getOrCreateCarvingMask(step);
        WorldgenRandom worldGenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        BiomeManager differentBiomeManager = biomeManager.withDifferentSource((x, y, z) -> this.biomeSource.getNoiseBiome(x, y, z, random.sampler()));
        CarvingContext carvingContext = new CarvingContext(this, level.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk, random, this.settings.value().surfaceRule());

        int i = 8;
        for (int j = -i; j <= i; ++j) {
            for (int k = -i; k <= i; ++k) {
                ChunkPos tempChunkPos = new ChunkPos(chunkPos.x + j, chunkPos.z + k);
                //noinspection deprecation
                BiomeGenerationSettings biomeGenerationSettings = level.getChunk(tempChunkPos.x, tempChunkPos.z).carverBiome(() -> {
                    //noinspection deprecation
                    return this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(tempChunkPos.getMinBlockX()), 0, QuartPos.fromBlock(tempChunkPos.getMinBlockZ()), random.sampler()));
                });

                int l = 0;
                for (Holder<ConfiguredWorldCarver<?>> holder : biomeGenerationSettings.getCarvers(step)) {
                    // my change
                    if (holder.unwrapKey().isPresent() && !ConfigHandler.World.carvers.get(this.dimension.location().toString()).test(holder.unwrapKey().get().location())) {
                        continue;
                    }

                    ConfiguredWorldCarver<?> configuredCarver = holder.value();
                    worldGenRandom.setLargeFeatureSeed(seed + (long) l, tempChunkPos.x, tempChunkPos.z);
                    if (configuredCarver.isStartChunk(worldGenRandom)) {
                        configuredCarver.carve(carvingContext, chunk, differentBiomeManager::getBiome, worldGenRandom, aquifer, tempChunkPos, carvingMask);
                    }

                    l++;
                }
            }
        }
    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level, @Nonnull RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    // [Vanilla copy] to ignore some features
    @Override
    public void applyBiomeDecoration(@Nonnull WorldGenLevel level, @Nonnull ChunkAccess chunk, @Nonnull StructureManager structureManager) {
        ChunkPos chunkPos = chunk.getPos();
        SectionPos sectionPos = SectionPos.of(chunkPos, level.getMinSection());
        BlockPos blockpos = sectionPos.origin();

        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        Map<Integer, List<Structure>> map = structureRegistry.stream().collect(Collectors.groupingBy(structure -> structure.step().ordinal()));
        List<FeatureSorter.StepFeatureData> stepFeatureDataList = this.featuresPerStep.get();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long decorationSeed = worldgenRandom.setDecorationSeed(level.getSeed(), blockpos.getX(), blockpos.getZ());

        Set<Holder<Biome>> possibleBiomes = new ObjectArraySet<>();
        ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach((pos) -> {
            ChunkAccess chunkaccess = level.getChunk(pos.x, pos.z);
            for (LevelChunkSection chunkSection : chunkaccess.getSections()) {
                chunkSection.getBiomes().getAll(possibleBiomes::add);
            }
        });
        possibleBiomes.retainAll(this.biomeSource.possibleBiomes());

        int dataSize = stepFeatureDataList.size();

        try {
            Registry<PlacedFeature> placedFeatureRegistry = level.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            int maxDecorations = Math.max(GenerationStep.Decoration.values().length, dataSize);

            for (int i = 0; i < maxDecorations; ++i) {
                int index = 0;
                if (structureManager.shouldGenerateStructures()) {
                    for (Structure structure : map.getOrDefault(i, Collections.emptyList())) {
                        ResourceLocation location = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).getKey(structure);
                        if (!ConfigHandler.Structures.generationStructures.test(location)) {
                            continue;
                        }

                        worldgenRandom.setFeatureSeed(decorationSeed, index, i);
                        Supplier<String> currentlyGenerating = () -> structureRegistry.getResourceKey(structure).map(Object::toString).orElseGet(structure::toString);

                        try {
                            level.setCurrentlyGenerating(currentlyGenerating);
                            structureManager.startsForStructure(sectionPos, structure).forEach((structureStart) -> {
                                structureStart.placeInChunk(level, structureManager, this, worldgenRandom, getWritableArea(chunk), chunkPos);
                            });
                        } catch (Exception e) {
                            CrashReport report = CrashReport.forThrowable(e, "Feature placement");
                            report.addCategory("Feature").setDetail("Description", currentlyGenerating::get);
                            throw new ReportedException(report);
                        }

                        ++index;
                    }
                }

                if (i < dataSize) {
                    IntSet mapping = new IntArraySet();

                    for (Holder<Biome> holder : possibleBiomes) {
                        List<HolderSet<PlacedFeature>> holderSets = this.generationSettingsGetter.apply(holder).features();
                        if (i < holderSets.size()) {
                            HolderSet<PlacedFeature> featureHolderSet = holderSets.get(i);
                            FeatureSorter.StepFeatureData stepFeatureData = stepFeatureDataList.get(i);
                            featureHolderSet.stream().map(Holder::value).forEach((p_223174_) -> {
                                mapping.add(stepFeatureData.indexMapping().applyAsInt(p_223174_));
                            });
                        }
                    }

                    int mappingSize = mapping.size();
                    int[] array = mapping.toIntArray();
                    Arrays.sort(array);
                    FeatureSorter.StepFeatureData stepFeatureData = stepFeatureDataList.get(i);

                    for (int j = 0; j < mappingSize; ++j) {
                        int featureIndex = array[j];
                        PlacedFeature placedfeature = stepFeatureData.features().get(featureIndex);
                        // The only reason why I needed to copy the code - checking if it should be placed
                        Optional<ResourceKey<ConfiguredFeature<?, ?>>> optionalResourceKey = placedfeature.feature().unwrapKey();
                        if (optionalResourceKey.isPresent() && !ConfigHandler.Structures.generationFeatures.test(optionalResourceKey.get().location())) {
                            continue;
                        }

                        Supplier<String> currentlyGenerating = () -> placedFeatureRegistry.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
                        worldgenRandom.setFeatureSeed(decorationSeed, featureIndex, i);

                        try {
                            level.setCurrentlyGenerating(currentlyGenerating);
                            placedfeature.placeWithBiomeCheck(level, this, worldgenRandom, blockpos);
                        } catch (Exception e) {
                            CrashReport report = CrashReport.forThrowable(e, "Feature placement");
                            report.addCategory("Feature").setDetail("Description", currentlyGenerating::get);
                            throw new ReportedException(report);
                        }
                    }
                }
            }

            level.setCurrentlyGenerating(null);
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Biome decoration");
            report.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Seed", decorationSeed);
            throw new ReportedException(report);
        }
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public List<FlatLayerInfo> getLayerInfos() {
        return this.layerInfos;
    }
}
