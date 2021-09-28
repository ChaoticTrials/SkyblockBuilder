package de.melanx.skyblockbuilder.world.dimensions.end;

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
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SkyblockEndBiomeProvider extends BiomeSource {

    public static final Codec<SkyblockEndBiomeProvider> CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter((provider) -> provider.seed),
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry)
            ).apply(builder, builder.stable((seed, lookupRegistry) -> new SkyblockEndBiomeProvider(
                    new TheEndBiomeSource(new LazyBiomeRegistryWrapper(lookupRegistry), seed)
            ))));

    private final TheEndBiomeSource parent;
    private final long seed;
    private final boolean isSingleBiomeLevel;
    public final Registry<Biome> lookupRegistry;

    public SkyblockEndBiomeProvider(TheEndBiomeSource parent) {
        super(parent.possibleBiomes());
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = parent.biomes;
        this.isSingleBiomeLevel = ConfigHandler.World.SingleBiome.enabled &&
                ConfigHandler.World.SingleBiome.singleBiomeDimension
                        .map(dimension -> dimension.getLocation().equals(WorldUtil.Dimension.THE_END.getLocation()))
                        .orElseGet(() -> ConfigHandler.Spawn.dimension.getLocation().equals(WorldUtil.Dimension.THE_END.getLocation()));
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
        return new SkyblockEndBiomeProvider((TheEndBiomeSource) this.parent.withSeed(seed));
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.THE_END.location());
            }
            return Objects.requireNonNull(biome);
        } else {
            return this.parent.getNoiseBiome(x, y, z);
        }
    }
}
