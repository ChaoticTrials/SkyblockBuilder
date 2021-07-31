package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockBiomeProvider;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
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
    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
        return configuredOverworldChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed);
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<Biome> biomeRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

        MappedRegistry<LevelStem> dimensions = WorldGenSettings.withOverworld(
                dimensionTypeRegistry,
                voidDimensions(biomeRegistry, dimensionSettingsRegistry, seed),
                this.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, null)
        );

        return new WorldGenSettings(seed, generateStructures, generateLoot, dimensions);
    }

    public static MappedRegistry<LevelStem> voidDimensions(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MappedRegistry<LevelStem> registry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        LazyBiomeRegistryWrapper biomes = new LazyBiomeRegistryWrapper(biomeRegistry);
        registry.register(LevelStem.OVERWORLD, new LevelStem(() -> DimensionType.DEFAULT_OVERWORLD,
                configuredOverworldChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(LevelStem.NETHER, new LevelStem(() -> DimensionType.DEFAULT_NETHER,
                ConfigHandler.Dimensions.Nether.Default ? DimensionType.defaultNetherGenerator(biomeRegistry, dimensionSettingsRegistry, seed)
                        : netherChunkGenerator(biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(LevelStem.END, new LevelStem(() -> DimensionType.DEFAULT_END,
                ConfigHandler.Dimensions.End.Default ? DimensionType.defaultEndGenerator(biomeRegistry, dimensionSettingsRegistry, seed)
                        : endChunkGenerator(biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        return registry;
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        return ConfigHandler.Dimensions.Overworld.Default ? WorldGenSettings.makeDefaultOverworld(biomeRegistry, dimensionSettingsRegistry, seed)
                : overworldChunkGenerator(new LazyBiomeRegistryWrapper(biomeRegistry), dimensionSettingsRegistry, seed);
    }

    public static ChunkGenerator overworldChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        OverworldBiomeSource overworld = new OverworldBiomeSource(seed, false, false, biomeRegistry);
        BiomeSource provider = new SkyblockBiomeProvider(overworld);
        NoiseGeneratorSettings settings = dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        return new SkyblockOverworldChunkGenerator(provider, seed, () -> settings);
    }

    private static ChunkGenerator netherChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource nether = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry, seed);
        SkyblockNetherBiomeProvider provider = new SkyblockNetherBiomeProvider(nether, biomeRegistry);

        NoiseGeneratorSettings settings = dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER);

        return new SkyblockNetherChunkGenerator(provider, seed, () -> settings);
    }

    private static ChunkGenerator endChunkGenerator(@Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        SkyblockEndBiomeProvider provider = new SkyblockEndBiomeProvider(new TheEndBiomeSource(biomeRegistry, seed));

        NoiseGeneratorSettings settings = dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(provider, seed, () -> settings);
    }
}
