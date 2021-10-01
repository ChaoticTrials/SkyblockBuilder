package de.melanx.skyblockbuilder.world.dimensions.end;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
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
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
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
                    DimensionSettings.DIMENSION_SETTINGS_CODEC.fieldOf("settings").forGetter((gen) -> gen.generatorSettings)
            ).apply(instance, instance.stable(SkyblockEndChunkGenerator::new)));

    protected final long seed;
    protected final Supplier<DimensionSettings> generatorSettings;
    protected final DimensionStructuresSettings settings;
    protected final NoiseChunkGenerator parent;

    public SkyblockEndChunkGenerator(BiomeProvider provider, long seed, Supplier<DimensionSettings> generatorSettings) {
        super(provider, provider, generatorSettings.get().getStructures(), seed);
        this.seed = seed;
        this.generatorSettings = generatorSettings;
        this.settings = RandomUtility.modifiedStructureSettings(generatorSettings.get().getStructures());
        this.parent = new NoiseChunkGenerator(provider, seed, generatorSettings);
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
        return new SkyblockEndChunkGenerator(this.biomeProvider.getBiomeProvider(newSeed), newSeed, this.generatorSettings);
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

    // Vanilla copy
    @Override
    public void addStructureStart(@Nonnull StructureFeature<?, ?> structure, @Nonnull DynamicRegistries dynamicRegistries, @Nonnull StructureManager structureManager, @Nonnull IChunk chunk, @Nonnull TemplateManager templateManager, long seed, @Nonnull ChunkPos chunkPos, @Nonnull Biome biome) {
        StructureStart<?> existingStart = structureManager.getStructureStart(SectionPos.from(chunk.getPos(), 0), structure.field_236268_b_, chunk);
        int i = existingStart != null ? existingStart.getRefCount() : 0;
        StructureSeparationSettings config = this.settings.getSeparationSettings(structure.field_236268_b_);
        if (config != null) {
            StructureStart<?> start = structure.func_242771_a(dynamicRegistries, this, this.biomeProvider, templateManager, seed, chunkPos, biome, i, config);
            structureManager.addStructureStart(SectionPos.from(chunk.getPos(), 0), structure.field_236268_b_, start, chunk);
        }
    }

    @Nonnull
    @Override
    public IBlockReader generateNoiseMap(int posX, int posZ) {
        return new Blockreader(new BlockState[0]);
    }
}
