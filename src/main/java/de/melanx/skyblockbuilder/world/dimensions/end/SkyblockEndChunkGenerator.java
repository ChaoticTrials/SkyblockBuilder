package de.melanx.skyblockbuilder.world.dimensions.end;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class SkyblockEndChunkGenerator extends SkyblockNoiseBasedChunkGenerator {

    public SkyblockEndChunkGenerator(Registry<NormalNoise.NoiseParameters> noises, BiomeSource provider, long seed, Supplier<NoiseGeneratorSettings> generatorSettings) {
        super(noises, provider, seed, generatorSettings, Level.END);
    }

    @Nonnull
    @Override
    public ChunkGenerator withSeed(long seed) {
        return new SkyblockEndChunkGenerator(this.noises, this.biomeSource.withSeed(seed), seed, this.generatorSettings);
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk) {
        super.buildSurface(level, structureManager, chunk);

        if (ConfigHandler.Dimensions.End.mainIsland) {
            this.parent.buildSurface(level, structureManager, chunk);
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 0 && chunkPos.z == 0) {
            chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.defaultBlockState(), false);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull Blender blender, @Nonnull StructureFeatureManager manager, @Nonnull ChunkAccess chunk) {
        if (ConfigHandler.Dimensions.End.mainIsland) {
            return this.parent.fillFromNoise(executor, blender, manager, chunk);
        }

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 0 && chunkPos.z == 0) {
            chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.defaultBlockState(), false);
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull BiomeManager biomeManager, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {
        ChunkPos pos = chunk.getPos();
        int value = 10 * 16;
        if (pos.getMinBlockX() < value && pos.getMinBlockX() > -value && pos.getMinBlockZ() < value && pos.getMinBlockZ() > -value) {
            super.applyCarvers(level, seed, biomeManager, structureManager, chunk, carving);
        }
    }

//    // Vanilla copy
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
}
