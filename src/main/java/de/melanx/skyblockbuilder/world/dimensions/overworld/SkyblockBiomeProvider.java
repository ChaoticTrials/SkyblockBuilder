package de.melanx.skyblockbuilder.world.dimensions.overworld;

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
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SkyblockBiomeProvider extends BiomeSource {

    // [VanillaCopy] overworld biome provider codec
    public static final Codec<SkyblockBiomeProvider> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter(provider -> provider.seed),
                    RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.lookupRegistry)
            ).apply(instance, instance.stable((seed, lookupRegistry) -> new SkyblockBiomeProvider(
                    new OverworldBiomeSource(seed, false, false, new LazyBiomeRegistryWrapper(lookupRegistry)))
            )));

    private final OverworldBiomeSource parent;
    private final boolean isSingleBiomeLevel;
    public final long seed;
    public final Registry<Biome> lookupRegistry;

    public SkyblockBiomeProvider(OverworldBiomeSource parent) {
        super(parent.possibleBiomes());
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = parent.biomes;
        this.isSingleBiomeLevel = ConfigHandler.World.SingleBiome.enabled &&
                ConfigHandler.World.SingleBiome.singleBiomeDimension.isPresent()
                ? ConfigHandler.World.SingleBiome.singleBiomeDimension.get().getLocation().equals(WorldUtil.Dimension.OVERWORLD.getLocation())
                : ConfigHandler.Spawn.dimension.getLocation().equals(WorldUtil.Dimension.OVERWORLD.getLocation());
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
        return new SkyblockBiomeProvider((OverworldBiomeSource) this.parent.withSeed(seed));
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        if (this.isSingleBiomeLevel) {
            Biome biome = this.lookupRegistry.get(WorldUtil.SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.get(Biomes.PLAINS.location());
            }
            return Objects.requireNonNull(biome);
        } else {
            int range = ConfigHandler.World.biomeRange;
            return this.parent.getNoiseBiome(((((x << 2) - range / 2) % range) + range) % range, y, ((((z << 2) - range / 2) % range) + range) % range);
        }
    }
}
