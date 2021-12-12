package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class SkyblockNetherBiomeProvider extends BiomeSource {

    public static final Codec<SkyblockNetherBiomeProvider> PACKET_CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry)
            ).apply(builder, (lookupRegistry) -> {
                LazyBiomeRegistryWrapper biomes = LazyBiomeRegistryWrapper.get(lookupRegistry);
                return new SkyblockNetherBiomeProvider(
                        MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomes), biomes
                );
            }));

    private final MultiNoiseBiomeSource parent;
    private final boolean isSingleBiomeLevel;
    public final Registry<Biome> lookupRegistry;

    public SkyblockNetherBiomeProvider(MultiNoiseBiomeSource parent, Registry<Biome> lookupRegistry) {
        super(List.copyOf(parent.possibleBiomes()));
        this.parent = parent;
        this.lookupRegistry = lookupRegistry;
        this.isSingleBiomeLevel = ConfigHandler.World.SingleBiome.enabled &&
                ConfigHandler.World.SingleBiome.singleBiomeDimension
                        .map(dimension -> dimension.getLocation().equals(WorldUtil.Dimension.THE_NETHER.getLocation()))
                        .orElseGet(() -> ConfigHandler.Spawn.dimension.getLocation().equals(WorldUtil.Dimension.THE_NETHER.getLocation()));
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return PACKET_CODEC;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public BiomeSource withSeed(long seed) {
        return new SkyblockNetherBiomeProvider((MultiNoiseBiomeSource) this.parent.withSeed(seed), this.lookupRegistry);
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.NETHER_WASTES.location());
            }
            return Objects.requireNonNull(biome);

            // TODO biome range
//        } else if (ConfigHandler.World.biomeRangeEnabled) {
//            int range = ConfigHandler.World.biomeRange / 8;
//            return this.parent.getNoiseBiome(((((x << 2) - range / 2) % range) + range) % range, y, ((((z << 2) - range / 2) % range) + range) % range, sampler);
        } else {
            return this.parent.getNoiseBiome(x, y, z, sampler);
        }
    }
}
