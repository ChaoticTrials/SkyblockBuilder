package de.melanx.skyblockbuilder.world.dimensions.nether;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
        this.lookupRegistry = lookupRegistry;

        this.lookupRegistry.getEntries().forEach(biomeEntry -> {
            // Remove non-whitelisted structures
            List<Supplier<StructureFeature<?, ?>>> structures = new ArrayList<>();
            for (Supplier<StructureFeature<?, ?>> structure : biomeEntry.getValue().getGenerationSettings().structures) {
                ResourceLocation location = structure.get().field_236268_b_.getRegistryName();
                if (location != null && ConfigHandler.whitelistStructures.get().contains(location.toString())) {
                    structures.add(structure);
                }
            }

            biomeEntry.getValue().getGenerationSettings().structures = ImmutableList.copyOf(structures);

            // Remove non-whitelisted features
            List<Supplier<ConfiguredFeature<?, ?>>> features = new ArrayList<>();
            biomeEntry.getValue().getGenerationSettings().features.forEach(list -> {
                for (Supplier<ConfiguredFeature<?, ?>> feature : list) {
                    ResourceLocation location = feature.get().feature.getRegistryName();
                    if (location != null && ConfigHandler.whitelistFeatures.get().contains(location.toString())) {
                        features.add(feature);
                    }
                }
            });

            biomeEntry.getValue().getGenerationSettings().features = ImmutableList.of(features);
        });
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
