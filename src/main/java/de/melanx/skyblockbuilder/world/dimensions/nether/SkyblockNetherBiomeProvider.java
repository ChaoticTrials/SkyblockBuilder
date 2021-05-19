package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SkyblockNetherBiomeProvider extends BiomeProvider {

    public static final Codec<SkyblockNetherBiomeProvider> PACKET_CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    Codec.LONG.fieldOf("seed").forGetter((provider) -> provider.seed),
                    RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter(provider -> provider.lookupRegistry)
            ).apply(builder, (seed, lookupRegistry) -> {
                LazyBiomeRegistryWrapper biomes = new LazyBiomeRegistryWrapper(lookupRegistry);
                return new SkyblockNetherBiomeProvider(
                        NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET.build(biomes, seed), biomes
                );
            }));

    private final NetherBiomeProvider parent;
    private final long seed;
    public final Registry<Biome> lookupRegistry;

    public SkyblockNetherBiomeProvider(NetherBiomeProvider parent, Registry<Biome> lookupRegistry) {
        super(parent.getBiomes());
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = lookupRegistry;
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeProvider> getBiomeProviderCodec() {
        return PACKET_CODEC;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public BiomeProvider getBiomeProvider(long seed) {
        return new SkyblockNetherBiomeProvider((NetherBiomeProvider) this.parent.getBiomeProvider(seed), this.lookupRegistry);
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        if (LibXConfigHandler.World.SingleBiome.enabled && LibXConfigHandler.World.SingleBiome.singleBiomeDimension.getDimension().equals(WorldUtil.SingleBiomeDimension.THE_NETHER.getDimension())) {
            Biome biome = this.lookupRegistry.getOrDefault(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.getOrDefault(Biomes.NETHER_WASTES.getLocation());
            }
            return Objects.requireNonNull(biome);
        } else {
            int range = LibXConfigHandler.World.biomeRange / 8;
            return this.parent.getNoiseBiome(((((x << 2) - range / 2) % range) + range) % range, y, ((((z << 2) - range / 2) % range) + range) % range);
        }
    }
}
