package de.melanx.skyblockbuilder.world.dimensions.end;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

public class SkyblockEndBiomeSource extends BiomeSource {

    public static final Codec<SkyblockEndBiomeSource> CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry),
                    Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed)
            ).apply(builder, builder.stable((lookupRegistry, seed) -> new SkyblockEndBiomeSource(
                    LazyBiomeRegistryWrapper.get(lookupRegistry), new TheEndBiomeSource(LazyBiomeRegistryWrapper.get(lookupRegistry), seed)
            ))));

    private final TheEndBiomeSource parent;
    private final long seed;
    private final boolean isSingleBiomeLevel;
    public final Holder<Biome> biome;
    public final Registry<Biome> lookupRegistry;

    public SkyblockEndBiomeSource(Registry<Biome> lookupRegistry, TheEndBiomeSource parent) {
        super(List.copyOf(parent.possibleBiomes()));
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = lookupRegistry;
        this.biome = lookupRegistry.getOrCreateHolder(ResourceKey.create(Registry.BIOME_REGISTRY, WorldUtil.SINGLE_BIOME));
        this.isSingleBiomeLevel = WorldUtil.isSingleBiomeLevel(Level.END);
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public BiomeSource withSeed(long seed) {
        return new SkyblockEndBiomeSource(this.lookupRegistry, (TheEndBiomeSource) this.parent.withSeed(seed));
    }

    @Nonnull
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        if (this.isSingleBiomeLevel) {
            return this.biome;
        } else {
            return this.parent.getNoiseBiome(x, y, z, sampler);
        }
    }
}
