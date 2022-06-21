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
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
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
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension)
                    )).apply(instance, instance.stable(SkyblockEndChunkGenerator::new)));

    public SkyblockEndChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension) {
        super(structureSets, noises, biomeSource, generatorSettings, dimension);
    }

    @Nonnull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return SkyblockEndChunkGenerator.CODEC;
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureManager structureManager, @Nonnull RandomState randomState, @Nonnull ChunkAccess chunk) {
        super.buildSurface(level, structureManager, randomState, chunk);

        if (ConfigHandler.Dimensions.End.mainIsland) {
            // [Vanilla copy] to use registry wrapper when calling SurfaceSystem#buildSurface
            WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, level);
            this.buildSurface(chunk, worldGenerationContext, randomState, structureManager, level.getBiomeManager(),
                    level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), Blender.of(level));
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x == 0 && chunkPos.z == 0) {
            chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.defaultBlockState(), false);
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Executor executor, @Nonnull Blender blender, @Nonnull RandomState randomState, @Nonnull StructureManager manager, @Nonnull ChunkAccess chunk) {
        if (ConfigHandler.Dimensions.End.mainIsland) {
            return this.parent.fillFromNoise(executor, blender, randomState, manager, chunk);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull RandomState randomState, @Nonnull BiomeManager biomeManager, @Nonnull StructureManager structureManager, @Nonnull ChunkAccess chunk, @Nonnull GenerationStep.Carving carving) {
        ChunkPos pos = chunk.getPos();
        int value = 10 * 16;
        if (pos.getMinBlockX() < value && pos.getMinBlockX() > -value && pos.getMinBlockZ() < value && pos.getMinBlockZ() > -value) {
            super.applyCarvers(level, seed, randomState, biomeManager, structureManager, chunk, carving);
        }
    }
}
