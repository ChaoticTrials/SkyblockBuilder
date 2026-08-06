package de.melanx.skyblockbuilder.world.chunkgenerators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.common.DimensionsConfig;
import de.melanx.skyblockbuilder.world.flat.FlatLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class SkyblockEndChunkGenerator extends SkyblockNoiseBasedChunkGenerator {

    private static final int MAIN_ISLAND_DISTANCE = 16;

    // [VanillaCopy] overworld chunk generator codec
    public static final MapCodec<SkyblockEndChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.generatorSettings),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(generator -> generator.dimension),
                    FlatLayers.CODEC.optionalFieldOf("layers", FlatLayers.EMPTY).forGetter(generator -> generator.flatLayers)
            ).apply(instance, instance.stable(SkyblockEndChunkGenerator::new)));

    public SkyblockEndChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> generatorSettings, ResourceKey<Level> dimension, FlatLayers flatLayers) {
        super(biomeSource, generatorSettings, dimension, flatLayers);
    }

    @Nonnull
    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return SkyblockEndChunkGenerator.CODEC;
    }

    @Override
    public void buildSurface(@Nonnull WorldGenRegion level, @Nonnull StructureManager structureManager, @Nonnull RandomState randomState, @Nonnull ChunkAccess chunk) {
        super.buildSurface(level, structureManager, randomState, chunk);

        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x() == 0 && chunkPos.z() == 0) {
            chunk.setBlockState(new BlockPos(0, 64, 0), Blocks.BEDROCK.defaultBlockState());
        }
    }

    @Nonnull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(@Nonnull Blender blender, @Nonnull RandomState randomState, @Nonnull StructureManager structureManager, @Nonnull ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        if (DimensionsConfig.End.keepMainIsland && Mth.abs(chunkPos.x()) <= MAIN_ISLAND_DISTANCE && Mth.abs(chunkPos.z()) <= MAIN_ISLAND_DISTANCE) {
            return this.parent.fillFromNoise(blender, randomState, structureManager, chunk);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(@Nonnull WorldGenRegion level, long seed, @Nonnull RandomState random, @Nonnull BiomeManager biomeManager, @Nonnull StructureManager structureManager, @Nonnull ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int value = MAIN_ISLAND_DISTANCE * 16;
        if (pos.getMinBlockX() <= value && pos.getMinBlockX() >= -value && pos.getMinBlockZ() <= value && pos.getMinBlockZ() >= -value) {
            super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk);
        }
    }
}
