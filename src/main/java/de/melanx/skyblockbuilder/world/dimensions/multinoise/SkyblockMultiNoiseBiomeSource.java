package de.melanx.skyblockbuilder.world.dimensions.multinoise;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.*;

import javax.annotation.Nonnull;
import java.util.Set;

public class SkyblockMultiNoiseBiomeSource extends MultiNoiseBiomeSource {

    public static final Codec<SkyblockMultiNoiseBiomeSource> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                    ExtraCodecs.nonEmptyList(RecordCodecBuilder.<Pair<Climate.ParameterPoint, Holder<Biome>>>create(inst -> inst
                                    .group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst),
                                            Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond))
                                    .apply(inst, Pair::of)
                            ).listOf())
                            .xmap(Climate.ParameterList::new, Climate.ParameterList::values)
                            .fieldOf("biomes")
                            .forGetter(source -> source.parameters),
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry),
                    Codec.BOOL.fieldOf("singleBiome").stable().forGetter(biomeSource -> biomeSource.isSingleBiomeLevel)
            ).apply(builder, builder.stable(
                    (parameters, lookupRegistry, isSingleBiomeLevel) -> {
                        return new SkyblockMultiNoiseBiomeSource(lookupRegistry, parameters, isSingleBiomeLevel);
                    })
            ));

    private final boolean isSingleBiomeLevel;
    public final LazyBiomeRegistryWrapper lookupRegistry;
    private Set<Holder<Biome>> modifiedPossibleBiomes;

    public SkyblockMultiNoiseBiomeSource(Registry<Biome> lookupRegistry, Climate.ParameterList<Holder<Biome>> parameters) {
        this(lookupRegistry, parameters, false);
    }

    public SkyblockMultiNoiseBiomeSource(Registry<Biome> lookupRegistry, Climate.ParameterList<Holder<Biome>> parameters, boolean isSingleBiomeLevel) {
        super(parameters);
        this.isSingleBiomeLevel = isSingleBiomeLevel;
        this.lookupRegistry = LazyBiomeRegistryWrapper.get(lookupRegistry);
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public Set<Holder<Biome>> possibleBiomes() {
        if (this.modifiedPossibleBiomes == null) {
            //noinspection OptionalGetWithoutIsPresent
            this.modifiedPossibleBiomes = new ObjectLinkedOpenHashSet<>(this.possibleBiomes.stream().map(holder -> this.lookupRegistry.getOrCreateHolder(this.lookupRegistry.getResourceKey(holder.value()).get())).distinct().toList());
        }

        return this.modifiedPossibleBiomes;
    }

    @Nonnull
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.PLAINS.location());
            }

            //noinspection OptionalGetWithoutIsPresent,ConstantConditions
            return this.lookupRegistry.getOrCreateHolder(this.lookupRegistry.getResourceKey(biome).get());
        } else {
            return this.lookupRegistry.modified(this.getNoiseBiome(sampler.sample(x, y, z)));
        }
    }
}
