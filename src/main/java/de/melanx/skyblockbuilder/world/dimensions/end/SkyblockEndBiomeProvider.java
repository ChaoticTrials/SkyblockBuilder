package de.melanx.skyblockbuilder.world.dimensions.end;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class SkyblockEndBiomeProvider extends BiomeProvider {
    
    public static final Codec<SkyblockEndBiomeProvider> CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed),
                    RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter(provider -> provider.lookupRegistry)
            ).apply(builder, builder.stable((seed, lookupRegistry) -> new SkyblockEndBiomeProvider(
                    new EndBiomeProvider(lookupRegistry, seed)
            ))));

    private final EndBiomeProvider parent;
    private final long seed;
    public final Registry<Biome> lookupRegistry;

    public SkyblockEndBiomeProvider(EndBiomeProvider parent) {
        super(parent.getBiomes());
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = new LazyBiomeRegistryWrapper(parent.lookupRegistry);
        parent.lookupRegistry = this.lookupRegistry;
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeProvider> getBiomeProviderCodec() {
        return CODEC;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public BiomeProvider getBiomeProvider(long seed) {
        return new SkyblockEndBiomeProvider((EndBiomeProvider) this.parent.getBiomeProvider(seed));
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return this.parent.getNoiseBiome(x, y, z);
    }
}
