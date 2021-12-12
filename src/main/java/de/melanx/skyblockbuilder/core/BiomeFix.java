package de.melanx.skyblockbuilder.core;

import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldBiomeSource;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

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
        if (biomeSource instanceof SkyblockOverworldBiomeSource || biomeSource instanceof SkyblockNetherBiomeSource) {
            return ChunkSerializer.makeBiomeCodec(LazyBiomeRegistryWrapper.get(biomeRegistry));
        }

        return ChunkSerializer.makeBiomeCodec(biomeRegistry);
    }
}
