package de.melanx.skyblockbuilder.world.dimensions.overworld;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

    public static void init() {
        Registry.register(Registry.BIOME_PROVIDER_CODEC, new ResourceLocation(SkyblockBuilder.MODID, "skyblock_provider"), SkyblockBiomeProvider.CODEC);
    }

    private final BiomeProvider parent;
    public final long seed;
    public final Registry<Biome> lookupRegistry;

    public SkyblockBiomeProvider(BiomeProvider parent) {
        super(parent.getBiomes());
        OverworldBiomeProvider provider = (OverworldBiomeProvider) parent;
        this.parent = parent;
        this.seed = provider.seed;
        this.lookupRegistry = provider.lookupRegistry;

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
        return CODEC;
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public BiomeProvider getBiomeProvider(long seed) {
        return new SkyblockBiomeProvider(this.parent.getBiomeProvider(seed));
    }

    @Nonnull
    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        if (ConfigHandler.singleBiome.get()) {
            Biome biome = this.lookupRegistry.getOrDefault(SINGLE_BIOME);
            if (biome == null) {
                biome = this.lookupRegistry.getOrDefault(Biomes.PLAINS.getLocation());
            }

            return biome;
        } else {
            return this.parent.getNoiseBiome(((((x << 2) - 4096) % 8192) + 8192) % 8192, y, ((((z << 2) - 4096) % 8192) + 8192) % 8192);
        }
    }
}
