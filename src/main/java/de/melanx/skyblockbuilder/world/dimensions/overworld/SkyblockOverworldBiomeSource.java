package de.melanx.skyblockbuilder.world.dimensions.overworld;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SkyblockOverworldBiomeSource extends BiomeSource {

    // [VanillaCopy] overworld biome source codec
    public static final Codec<SkyblockOverworldBiomeSource> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    ExtraCodecs.nonEmptyList(RecordCodecBuilder.<Pair<Climate.ParameterPoint, Supplier<Biome>>>create((inst) -> inst
                                    .group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond))
                                    .apply(inst, Pair::of)
                            ).listOf())
                            .xmap(Climate.ParameterList::new, Climate.ParameterList::values)
                            .fieldOf("biomes")
                            .forGetter((provider) -> provider.parameters),
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry)
            ).apply(instance, instance.stable((parameterList, lookupRegistry) -> new SkyblockOverworldBiomeSource(
                    lookupRegistry, new MultiNoiseBiomeSource(parameterList)
            ))));

    private final boolean isSingleBiomeLevel;
    public final MultiNoiseBiomeSource parent;
    public final Registry<Biome> lookupRegistry;
    protected final Climate.ParameterList<Supplier<Biome>> parameters;

    public SkyblockOverworldBiomeSource(Registry<Biome> lookupRegistry, MultiNoiseBiomeSource parent) {
        super(List.copyOf(parent.possibleBiomes()));
        this.parent = parent;
        this.parameters = parent.parameters;
        this.lookupRegistry = LazyBiomeRegistryWrapper.get(lookupRegistry);
        this.isSingleBiomeLevel = ConfigHandler.World.SingleBiome.enabled &&
                ConfigHandler.World.SingleBiome.singleBiomeDimension
                        .map(value -> value.getLocation().equals(WorldUtil.Dimension.OVERWORLD.getLocation()))
                        .orElseGet(() -> ConfigHandler.Spawn.dimension.getLocation().equals(WorldUtil.Dimension.OVERWORLD.getLocation()));
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public BiomeSource withSeed(long seed) {
        return new SkyblockOverworldBiomeSource(this.lookupRegistry, (MultiNoiseBiomeSource) this.parent.withSeed(seed));
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
