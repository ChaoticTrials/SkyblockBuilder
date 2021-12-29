package de.melanx.skyblockbuilder.world.dimensions.end;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class SkyblockEndBiomeSource extends BiomeSource {

    public static final Codec<SkyblockEndBiomeSource> CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry),
                    Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed)
            ).apply(builder, builder.stable((lookupRegistry, seed) -> new SkyblockEndBiomeSource(
                    new TheEndBiomeSource(LazyBiomeRegistryWrapper.get(lookupRegistry), seed)
            ))));

    private final TheEndBiomeSource parent;
    private final long seed;
    private final boolean isSingleBiomeLevel;
    public final Registry<Biome> lookupRegistry;

    public SkyblockEndBiomeSource(TheEndBiomeSource parent) {
        super(List.copyOf(parent.possibleBiomes()));
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = parent.biomes;
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
        return new SkyblockEndBiomeSource((TheEndBiomeSource) this.parent.withSeed(seed));
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.THE_END.location());
            }
            return Objects.requireNonNull(biome);
        } else {
            return this.parent.getNoiseBiome(x, y, z, sampler);
        }
    }
}
