package de.melanx.skyblockbuilder.world.dimensions.overworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SkyblockBiomeProvider extends BiomeProvider {

    // [VanillaCopy] overworld biome provider codec
    public static final Codec<SkyblockBiomeProvider> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter(provider -> provider.seed),
                    RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter(provider -> provider.lookupRegistry)
            ).apply(instance, instance.stable((seed, lookupRegistry) -> new SkyblockBiomeProvider(
                    new OverworldBiomeProvider(seed, false, false, lookupRegistry))
            )));
    public static final ResourceLocation SINGLE_BIOME = ResourceLocation.tryCreate(ConfigHandler.biome.get());

    private final OverworldBiomeProvider parent;
    public final long seed;
    public final Registry<Biome> lookupRegistry;

    public SkyblockBiomeProvider(OverworldBiomeProvider parent) {
        super(parent.getBiomes());
        this.parent = parent;
        this.seed = parent.seed;
        this.lookupRegistry = parent.lookupRegistry;
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeProvider> getBiomeProviderCodec() {
        return CODEC;
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public BiomeProvider getBiomeProvider(long seed) {
        return new SkyblockBiomeProvider((OverworldBiomeProvider) this.parent.getBiomeProvider(seed));
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        if (ConfigHandler.singleBiome.get()) {
            Biome biome = this.lookupRegistry.getOrDefault(SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.getOrDefault(Biomes.PLAINS.getLocation());
            }
            return Objects.requireNonNull(biome);
        } else {
            int range = ConfigHandler.biomeRange.get();
            return this.parent.getNoiseBiome(((((x << 2) - range / 2) % range) + range) % range, y, ((((z << 2) - range / 2) % range) + range) % range);
        }
    }
}
