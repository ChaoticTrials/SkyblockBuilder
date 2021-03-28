package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class SkyblockNetherBiomeProvider extends BiomeProvider {
    
    public static final Codec<SkyblockNetherBiomeProvider> PACKET_CODEC = RecordCodecBuilder.create(
            (builder) -> builder.group(
                    Codec.LONG.fieldOf("seed").forGetter((provider) -> provider.seed),
                    RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter(provider -> provider.lookupRegistry)
            ).apply(builder, (seed, lookupRegistry) -> new SkyblockNetherBiomeProvider(
                    NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET.build(lookupRegistry, seed), lookupRegistry
            )));

    public static void init() {
        Registry.register(Registry.BIOME_PROVIDER_CODEC, new ResourceLocation(SkyblockBuilder.MODID, "skyblock_nether_provider"), SkyblockNetherBiomeProvider.PACKET_CODEC);
    }

    private final BiomeProvider parent;
    private final long seed;
    public final Registry<Biome> lookupRegistry;

    public SkyblockNetherBiomeProvider(BiomeProvider parent, Registry<Biome> lookupRegistry) {
        super(parent.getBiomes());
        NetherBiomeProvider provider = (NetherBiomeProvider) parent;
        this.parent = parent;
        this.seed = provider.seed;
        this.lookupRegistry = RandomUtility.modifyLookupRegistry(lookupRegistry);
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
        return new SkyblockNetherBiomeProvider(this.parent.getBiomeProvider(seed), this.lookupRegistry);
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        int range = ConfigHandler.biomeRange.get() / 8;
        return this.parent.getNoiseBiome(((((x << 2) - range / 2) % range) + range) % range, y, ((((z << 2) - range / 2) % range) + range) % range);
    }
}
