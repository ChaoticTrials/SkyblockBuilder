package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SkyblockNetherBiomeProvider extends BiomeSource {

    public static final Codec<SkyblockNetherBiomeProvider> PACKET_CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    Codec.LONG.fieldOf("seed").forGetter((provider) -> provider.seed),
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry)
            ).apply(builder, (seed, lookupRegistry) -> {
                LazyBiomeRegistryWrapper biomes = new LazyBiomeRegistryWrapper(lookupRegistry);
                return new SkyblockNetherBiomeProvider(
                        MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomes, seed), biomes
                );
            }));

    private final MultiNoiseBiomeSource parent;
    private final long seed;
    private final boolean isSingleBiomeLevel;
    public final Registry<Biome> lookupRegistry;

    public SkyblockNetherBiomeProvider(MultiNoiseBiomeSource parent, Registry<Biome> lookupRegistry) {
        super(parent.possibleBiomes());
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = lookupRegistry;
        this.isSingleBiomeLevel = ConfigHandler.World.SingleBiome.enabled &&
                ConfigHandler.World.SingleBiome.singleBiomeDimension.isPresent()
                ? ConfigHandler.World.SingleBiome.singleBiomeDimension.get().getLocation().equals(WorldUtil.Dimension.THE_NETHER.getLocation())
                : ConfigHandler.Spawn.dimension.getLocation().equals(WorldUtil.Dimension.THE_NETHER.getLocation());
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
    public Biome getNoiseBiome(int x, int y, int z) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.NETHER_WASTES.location());
            }
            return Objects.requireNonNull(biome);
        } else {
            int range = ConfigHandler.World.biomeRange / 8;
            return this.parent.getNoiseBiome(((((x << 2) - range / 2) % range) + range) % range, y, ((((z << 2) - range / 2) % range) + range) % range);
        }
    }
}
