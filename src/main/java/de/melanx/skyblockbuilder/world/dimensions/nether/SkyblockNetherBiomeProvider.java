package de.melanx.skyblockbuilder.world.dimensions.nether;

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
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
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
        if (ConfigHandler.netherStructures.get() && (ConfigHandler.disableFortress.get() || ConfigHandler.disableBastion.get())) {
            this.lookupRegistry.getEntries().forEach(biomeEntry -> {
                if (biomeEntry.getValue().getCategory() == Biome.Category.NETHER) {
                    List<Supplier<StructureFeature<?, ?>>> newStructures = new ArrayList<>();
                    for (Supplier<StructureFeature<?, ?>> structure : biomeEntry.getValue().getGenerationSettings().structures) {
                        if (structure.get().field_236268_b_ == Structure.FORTRESS) {
                            if (!ConfigHandler.disableFortress.get()) {
                                newStructures.add(structure);
                                continue;
                            }
                        }

                        if (structure.get().field_236268_b_ == Structure.BASTION_REMNANT) {
                            if (!ConfigHandler.disableBastion.get()) {
                                newStructures.add(structure);
                            }
                        }
                    }

                    biomeEntry.getValue().getGenerationSettings().structures = newStructures;
                }
            });
        }
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
        return this.parent.getNoiseBiome(((((x << 2) - 512) % 1024) + 1024) % 1024, y, ((((z << 2) - 512) % 1024) + 1024) % 1024);
    }
}
