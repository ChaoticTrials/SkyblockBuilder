package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.LibXConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;

import javax.annotation.Nonnull;

public class VoidWorldType extends ForgeWorldType {

    public VoidWorldType() {
        super(VoidWorldType::configuredOverworldChunkGenerator);
    }

    @Override
    public String getTranslationKey() {
        return SkyblockBuilder.getInstance().modid + ".generator.custom_skyblock";
    }

    @Override
    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
        return configuredOverworldChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed);
    }

    @Override
    public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<Biome> biomeRegistry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY);

        SimpleRegistry<Dimension> dimensions = DimensionGeneratorSettings.func_242749_a(
                dimensionTypeRegistry,
                voidDimensions(biomeRegistry, dimensionSettingsRegistry, seed),
                this.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, null)
        );

        return new DimensionGeneratorSettings(seed, generateStructures, generateLoot, dimensions);
    }

    @Deprecated // use without DynamicRegistries instead
    public static SimpleRegistry<Dimension> voidDimensions(DynamicRegistries dynamicRegistries, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        return voidDimensions(biomeRegistry, dimensionSettingsRegistry, seed);
    }

    public static SimpleRegistry<Dimension> voidDimensions(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        SimpleRegistry<Dimension> registry = new SimpleRegistry<>(Registry.DIMENSION_KEY, Lifecycle.experimental());
        LazyBiomeRegistryWrapper biomes = new LazyBiomeRegistryWrapper(biomeRegistry);
        registry.register(Dimension.OVERWORLD, new Dimension(() -> DimensionType.OVERWORLD_TYPE,
                configuredOverworldChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(Dimension.THE_NETHER, new Dimension(() -> DimensionType.NETHER_TYPE,
                LibXConfigHandler.Dimensions.Nether.Default ? DimensionType.getNetherChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)
                        : netherChunkGenerator(biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(Dimension.THE_END, new Dimension(() -> DimensionType.END_TYPE,
                LibXConfigHandler.Dimensions.End.Default ? DimensionType.getEndChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)
                        : endChunkGenerator(biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        return registry;
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        return LibXConfigHandler.Dimensions.Overworld.Default ? DimensionGeneratorSettings.func_242750_a(biomeRegistry, dimensionSettingsRegistry, seed)
                : overworldChunkGenerator(new LazyBiomeRegistryWrapper(biomeRegistry), dimensionSettingsRegistry, seed);
    }

    public static ChunkGenerator overworldChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        OverworldBiomeProvider overworld = new OverworldBiomeProvider(seed, false, false, biomeRegistry);
        BiomeProvider provider = new SkyblockBiomeProvider(overworld);
        DimensionSettings settings = dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242734_c);

        return new SkyblockOverworldChunkGenerator(provider, seed, () -> settings);
    }

    private static ChunkGenerator netherChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        NetherBiomeProvider nether = NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET.build(biomeRegistry, seed);
        SkyblockNetherBiomeProvider provider = new SkyblockNetherBiomeProvider(nether, biomeRegistry);

        DimensionSettings settings = dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242736_e);

        return new SkyblockNetherChunkGenerator(provider, seed, () -> settings);
    }

    private static ChunkGenerator endChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        SkyblockEndBiomeProvider provider = new SkyblockEndBiomeProvider(new EndBiomeProvider(biomeRegistry, seed));

        DimensionSettings settings = dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242737_f);

        return new SkyblockEndChunkGenerator(provider, seed, () -> settings);
    }
}
