package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.world.end.SkyblockEndBiomeProvider;
import de.melanx.skyblockbuilder.world.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.overworld.SkyblockOverworldChunkGenerator;
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
import net.minecraftforge.event.RegistryEvent;

import javax.annotation.Nonnull;

public class VoidWorldType extends ForgeWorldType {
    public VoidWorldType() {
        super(VoidWorldType::overworldChunkGenerator);
    }

    public static void register(RegistryEvent.Register<ForgeWorldType> event) {
        event.getRegistry().register(new VoidWorldType().setRegistryName("custom_skyblock"));
    }

    @Override
    public String getTranslationKey() {
        return SkyblockBuilder.MODID + ".generator.custom_skyblock";
    }

    @Override
    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
        return overworldChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed);
    }

    @Override
    public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<Biome> biomeRegistry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY);

        SimpleRegistry<Dimension> dimensions = DimensionGeneratorSettings.func_242749_a(
                dimensionTypeRegistry,
                voidDimensions(dynamicRegistries, biomeRegistry, dimensionSettingsRegistry, seed),
                this.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, null)
        );

        return new DimensionGeneratorSettings(seed, generateStructures, generateLoot, dimensions);
    }

    public static SimpleRegistry<Dimension> voidDimensions(DynamicRegistries dynamicRegistries, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        SimpleRegistry<Dimension> registry = new SimpleRegistry<>(Registry.DIMENSION_KEY, Lifecycle.experimental());
        registry.register(Dimension.OVERWORLD, new Dimension(() -> DimensionType.OVERWORLD_TYPE, overworldChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(Dimension.THE_NETHER, new Dimension(() -> DimensionType.NETHER_TYPE, netherChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(Dimension.THE_END, new Dimension(() -> DimensionType.END_TYPE,
                ConfigHandler.defaultEnd.get() ? DimensionType.getEndChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)
                        : endChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        return registry;
    }

    private static ChunkGenerator overworldChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        BiomeProvider overworld = new OverworldBiomeProvider(seed, false, false, biomeRegistry);
        BiomeProvider provider = new SkyblockBiomeProvider(overworld);

        return new SkyblockOverworldChunkGenerator(provider, seed, () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242734_c));
    }

    private static ChunkGenerator netherChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        NetherBiomeProvider nether = NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET.build(biomeRegistry, seed);
        SkyblockNetherBiomeProvider provider = new SkyblockNetherBiomeProvider(nether, biomeRegistry);

        return new SkyblockNetherChunkGenerator(provider, seed, () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242736_e));
    }

    private static ChunkGenerator endChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<DimensionSettings> dimensionSettingsRegistry, long seed) {
        SkyblockEndBiomeProvider provider = new SkyblockEndBiomeProvider(new EndBiomeProvider(biomeRegistry, seed));

        return new SkyblockEndChunkGenerator(provider, seed, () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242737_f));
    }
}
