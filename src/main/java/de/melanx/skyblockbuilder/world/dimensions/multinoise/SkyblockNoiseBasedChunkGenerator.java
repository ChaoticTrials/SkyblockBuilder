package de.melanx.skyblockbuilder.world.dimensions.multinoise;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.RegistryLookupCodec;
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
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SkyblockNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    RegistryLookupCodec.create(Registry.NOISE_REGISTRY).forGetter(generator -> generator.noises),
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension)
            ).apply(instance, instance.stable(SkyblockNoiseBasedChunkGenerator::new)));

    protected final long seed;
    protected final Registry<NormalNoise.NoiseParameters> noises;
    protected final Supplier<NoiseGeneratorSettings> generatorSettings;
    protected final StructureSettings settings;
    protected final NoiseBasedChunkGenerator parent;
    protected final ResourceKey<Level> dimension;
    protected final List<FlatLayerInfo> layerInfos;
    private final int layerHeight;

    public SkyblockNoiseBasedChunkGenerator(Registry<NormalNoise.NoiseParameters> noises, BiomeSource source, long seed, Supplier<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension) {
        super(noises, source, seed, generatorSettings);
        this.seed = seed;
        this.noises = noises;
        this.generatorSettings = generatorSettings;
        this.settings = RandomUtility.modifiedStructureSettings(generatorSettings.get().structureSettings());
        this.parent = new NoiseBasedChunkGenerator(this.noises, source, seed, generatorSettings);
        this.dimension = dimension;
        this.layerInfos = ConfigHandler.World.surface
                ? WorldUtil.layersInfoFromString(ConfigHandler.World.surfaceSettings.get(dimension.location().toString()))
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
        return new SkyblockNoiseBasedChunkGenerator(this.noises, this.biomeSource.withSeed(seed), seed, this.generatorSettings, this.dimension);
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

    // [Vanilla Copy]
    @Nullable
    @Override
    public BlockPos findNearestMapFeature(@Nonnull ServerLevel level, @Nonnull StructureFeature<?> structure, @Nonnull BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        boolean shouldSearch = this.generatorSettings.get().structureSettings.structureConfig().get(structure) != null;

        if (!shouldSearch) {
            return null;
        }

        if (structure == StructureFeature.STRONGHOLD) {
            this.generateStrongholds();
            BlockPos startPos = null;
            double currentDist = Double.MAX_VALUE;
            BlockPos.MutableBlockPos potentialStartPos = new BlockPos.MutableBlockPos();

            for (ChunkPos chunkPos : this.strongholdPositions) {
                potentialStartPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
                double newDist = potentialStartPos.distSqr(pos);
                if (startPos == null) {
                    startPos = potentialStartPos.immutable();
                    currentDist = newDist;
                } else if (newDist < currentDist) {
                    startPos = potentialStartPos.immutable();
                    currentDist = newDist;
                }
            }

            return startPos;
        }

        StructureFeatureConfiguration config = this.settings.getConfig(structure);
        ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> featureBiomeMap = this.settings.structures(structure);

        // my change: get correct biome registry
        Registry<Biome> registry;
        if (this.biomeSource instanceof SkyblockMultiNoiseBiomeSource biomeSource) {
            registry = biomeSource.lookupRegistry;
        } else {
            registry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        }

        if (config != null && !featureBiomeMap.isEmpty()) {
            Set<ResourceKey<Biome>> biomeKeys = this.runtimeBiomeSource.possibleBiomes()
                    .stream()
                    .flatMap((biome) -> registry.getResourceKey(biome).stream())
                    .collect(Collectors.toSet());
            return featureBiomeMap.values()
                    .stream()
                    .noneMatch(biomeKeys::contains) ? null : structure.getNearestGeneratedFeature(level, level.structureFeatureManager(), pos, searchRadius, skipKnownStructures, level.getSeed(), config);
        }

        return null;
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

    // Vanilla copy
    @Override
    public void createStructures(@Nonnull RegistryAccess registry, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull StructureManager templateManager, long seed) {
        ChunkPos chunkpos = chunk.getPos();
        SectionPos sectionpos = SectionPos.bottomOf(chunk);
        StructureFeatureConfiguration strongholdConfig = this.settings.getConfig(StructureFeature.STRONGHOLD);
        if (strongholdConfig != null) {
            StructureStart<?> existingStart = structureManager.getStartForFeature(sectionpos, StructureFeature.STRONGHOLD, chunk);
            if (WorldUtil.isStructureGenerated(StructureFeature.STRONGHOLD.getRegistryName()) && (existingStart == null || !existingStart.isValid())) {
                StructureStart<?> start = StructureFeatures.STRONGHOLD.generate(registry, this, this.biomeSource, templateManager, seed, chunkpos, ChunkGenerator.fetchReferences(structureManager, chunk, sectionpos, StructureFeature.STRONGHOLD), strongholdConfig, chunk, ChunkGenerator::validStrongholdBiome);
                structureManager.setStartForFeature(sectionpos, StructureFeature.STRONGHOLD, start, chunk);
            }
        }

        Registry<Biome> biomeRegistry = LazyBiomeRegistryWrapper.get(registry.registryOrThrow(Registry.BIOME_REGISTRY));

        structuresLoop:
        for (StructureFeature<?> structureFeature : ForgeRegistries.STRUCTURE_FEATURES) {
            if (structureFeature == StructureFeature.STRONGHOLD) {
                continue;
            }

            StructureFeatureConfiguration config = this.settings.getConfig(structureFeature);
            if (config != null) {
                StructureStart<?> existingStart = structureManager.getStartForFeature(sectionpos, structureFeature, chunk);

                if (WorldUtil.isStructureGenerated(structureFeature.getRegistryName()) && (existingStart == null || !existingStart.isValid())) {
                    int references = ChunkGenerator.fetchReferences(structureManager, chunk, sectionpos, structureFeature);

                    for (Map.Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> entry : this.settings.structures(structureFeature).asMap().entrySet()) {
                        StructureStart<?> start = entry.getKey().generate(registry, this, this.biomeSource, templateManager, seed, chunkpos, references, config, chunk, (biome) -> {
                            return this.validBiome(biomeRegistry, entry.getValue()::contains, biome);
                        });

                        if (start.isValid()) {
                            structureManager.setStartForFeature(sectionpos, structureFeature, start, chunk);
                            continue structuresLoop;
                        }
                    }

                    structureManager.setStartForFeature(sectionpos, structureFeature, StructureStart.INVALID_START, chunk);
                }
            }
        }
    }

    @Nonnull
    @Override
    public NoiseColumn getBaseColumn(int posX, int posZ, @Nonnull LevelHeightAccessor level) {
        return new NoiseColumn(0, new BlockState[0]);
    }
}
