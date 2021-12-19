package de.melanx.skyblockbuilder.world.dimensions.multinoise;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SkyblockMultiNoiseBiomeSource extends BiomeSource {

    public static final Codec<SkyblockMultiNoiseBiomeSource> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                    ExtraCodecs.nonEmptyList(RecordCodecBuilder.<Pair<Climate.ParameterPoint, Supplier<Biome>>>create((inst) -> inst
                                    .group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond))
                                    .apply(inst, Pair::of)
                            ).listOf())
                            .xmap(Climate.ParameterList::new, Climate.ParameterList::values)
                            .fieldOf("biomes")
                            .forGetter((provider) -> provider.parameters),
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry),
                    Codec.BOOL.fieldOf("singleBiome").stable().forGetter(biomeSource -> biomeSource.isSingleBiomeLevel)
            ).apply(builder, builder.stable(
                    (parameters, lookupRegistry, isSingleBiomeLevel) -> new SkyblockMultiNoiseBiomeSource(lookupRegistry, new MultiNoiseBiomeSource(parameters), isSingleBiomeLevel))
            ));

    private final boolean isSingleBiomeLevel;
    public final MultiNoiseBiomeSource parent;
    public final Registry<Biome> lookupRegistry;
    public final Climate.ParameterList<Supplier<Biome>> parameters;

    public SkyblockMultiNoiseBiomeSource(Registry<Biome> lookupRegistry, MultiNoiseBiomeSource parent) {
        this(lookupRegistry, parent, false);
    }

    public SkyblockMultiNoiseBiomeSource(Registry<Biome> lookupRegistry, MultiNoiseBiomeSource parent, boolean isSingleBiomeLevel) {
        super(List.copyOf(parent.possibleBiomes()));
        this.isSingleBiomeLevel = isSingleBiomeLevel;
        this.parent = parent;
        this.parameters = parent.parameters;
        this.lookupRegistry = LazyBiomeRegistryWrapper.get(lookupRegistry);
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public BiomeSource withSeed(long seed) {
        return new SkyblockMultiNoiseBiomeSource(this.lookupRegistry, (MultiNoiseBiomeSource) this.parent.withSeed(seed), this.isSingleBiomeLevel);
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.PLAINS.location());
            }
            return Objects.requireNonNull(biome);
            // TODO biome range
//        } else if (ConfigHandler.World.biomeRangeEnabled) {
//            int range = ConfigHandler.World.biomeRange >> 10;
//            int x2 = (((x - range / 2) % range) + range) % range;
//            int z2 = (((z - range / 2) % range) + range) % range;
//            return this.parent.getNoiseBiome(sampler.sample(x2, y, z2));
        } else {
            return this.parent.getNoiseBiome(x, y, z, sampler);
        }
    }
}
