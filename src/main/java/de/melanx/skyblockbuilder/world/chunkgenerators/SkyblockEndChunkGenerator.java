package de.melanx.skyblockbuilder.world.chunkgenerators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SkyblockEndChunkGenerator extends SkyblockNoiseBasedChunkGenerator {

    // [VanillaCopy] overworld chunk generator codec
    public static final Codec<SkyblockEndChunkGenerator> CODEC = RecordCodecBuilder.create(
            (instance) -> ChunkGenerator.commonCodec(instance)
                    .and(instance.group(
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(generator -> generator.noises),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension)
                    )).apply(instance, instance.stable(SkyblockEndChunkGenerator::new)));

    public SkyblockEndChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, long seed, Holder<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension) {
        super(structureSets, noises, biomeSource, seed, generatorSettings, dimension);
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return SkyblockEndChunkGenerator.CODEC;
    }

    @Nonnull
    @Override
    public ChunkGenerator withSeed(long seed) {
        return new SkyblockEndChunkGenerator(this.structureSets, this.noises, this.biomeSource.withSeed(seed), seed, this.generatorSettings, this.dimension);
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureFeatureManager structureManager, @Nonnull ChunkAccess chunk) {
        super.buildSurface(level, structureManager, chunk);

        if (ConfigHandler.Dimensions.End.mainIsland) {
            // [Vanilla copy] to use registry wrapper when calling SurfaceSystem#buildSurface
            WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, level);
            NoiseGeneratorSettings noiseGeneratorSettings = this.generatorSettings.value();
            NoiseChunk noisechunk = chunk.getOrCreateNoiseChunk(this.router(), () -> {
                //noinspection CodeBlock2Expr
                return new Beardifier(structureManager, chunk);
            }, noiseGeneratorSettings, this.globalFluidPicker, Blender.of(level));
            this.surfaceSystem.buildSurface(level.getBiomeManager(), level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
                    noiseGeneratorSettings.useLegacyRandomSource(), worldGenerationContext, chunk, noisechunk, noiseGeneratorSettings.surfaceRule());
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
}
