package de.melanx.skyblockbuilder.core;

import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockMultiNoiseBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

import java.util.Objects;

public class BiomeFix {

    /**
     * Patched into {@link LinearPalette#write(FriendlyByteBuf)}} redirecting the call to
     * {@link IdMap#getId(Object)} passing the {@link IdMap} reference and all arguments.
     */
    public static int getId(IdMap<Object> lookup, Object value) {
        if (value instanceof Biome biome) {
            int id = lookup.getId(biome);
            if (id >= 0) {
                return id;
            } else {
                return RandomUtility.validateBiome(biome);
            }
        }

        return lookup.getId(value);
    }

    /**
     * Patched into {@link ChunkSerializer#write(ServerLevel, ChunkAccess)} redirecting the call to
     * change the biome registry if needed.
     */
    public static Codec<PalettedContainer<Biome>> modifiedCodec(Registry<Biome> biomeRegistry, ServerLevel level) {
        BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();
        if (biomeSource instanceof SkyblockMultiNoiseBiomeSource || biomeSource instanceof SkyblockEndBiomeSource) {
            return ChunkSerializer.makeBiomeCodec(LazyBiomeRegistryWrapper.get(biomeRegistry));
        }

        return ChunkSerializer.makeBiomeCodec(biomeRegistry);
    }

    /**
     * Patched into {@link ChunkGenerator#findNearestMapFeature(ServerLevel, StructureFeature, BlockPos, int, boolean)}
     * redirecting to get the modified biome registry if needed to actually find the structure.
     */
    public static Registry<Biome> modifiedRegistry(Registry<Biome> biomeRegistry, ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        if (generator instanceof SkyblockNoiseBasedChunkGenerator) {
            return LazyBiomeRegistryWrapper.get(biomeRegistry);
        }

        return biomeRegistry;
    }

    /**
     * Patched at head of {@link ServerLevel#findNearestBiome(Biome, BlockPos, int, int)} to change the way how to
     * search for the biomes.
     */
    public static BlockPos findNearestBiome(ServerLevel level, Biome biome, BlockPos pos, int radius, int increment) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        if (generator instanceof SkyblockNoiseBasedChunkGenerator) {
            return generator.getBiomeSource().findBiomeHorizontal(pos.getX(), pos.getY(), pos.getZ(), radius, increment, target -> {
                return Objects.equals(target.getRegistryName(), biome.getRegistryName());
            }, level.random, true, generator.climateSampler());
        }

        return null;
    }
}
