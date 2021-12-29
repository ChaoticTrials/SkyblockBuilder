package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockMultiNoiseBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.common.world.ForgeWorldPreset;

import javax.annotation.Nonnull;

public class VoidWorldType extends ForgeWorldPreset {

    public VoidWorldType() {
        super(VoidWorldType::configuredOverworldChunkGenerator);
    }

    @Override
    public String getTranslationKey() {
        return SkyblockBuilder.getInstance().modid + ".generator.custom_skyblock";
    }

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess dynamicRegistries, long seed, String generatorSettings) {
        return configuredOverworldChunkGenerator(dynamicRegistries, seed);
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

        MappedRegistry<LevelStem> dimensions = WorldGenSettings.withOverworld(
                dimensionTypeRegistry,
                voidDimensions(dynamicRegistries, seed),
                this.createChunkGenerator(dynamicRegistries, seed, null)
        );

        return new WorldGenSettings(seed, generateStructures, generateLoot, dimensions);
    }

    public static MappedRegistry<LevelStem> voidDimensions(RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noiseRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        MappedRegistry<LevelStem> registry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        LazyBiomeRegistryWrapper biomes = LazyBiomeRegistryWrapper.get(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));

        registry.register(LevelStem.OVERWORLD, new LevelStem(() -> DimensionType.DEFAULT_OVERWORLD,
                configuredOverworldChunkGenerator(dynamicRegistries, seed)), Lifecycle.stable());

        registry.register(LevelStem.NETHER, new LevelStem(() -> DimensionType.DEFAULT_NETHER,
                ConfigHandler.Dimensions.Nether.Default ? VoidWorldType.defaultNetherGenerator(dynamicRegistries, seed)
                        : netherChunkGenerator(noiseRegistry, biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());

        registry.register(LevelStem.END, new LevelStem(() -> DimensionType.DEFAULT_END,
                ConfigHandler.Dimensions.End.Default ? VoidWorldType.defaultEndGenerator(dynamicRegistries, seed)
                        : endChunkGenerator(noiseRegistry, biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        return registry;
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(RegistryAccess dynamicRegistries, long seed) {
        LazyBiomeRegistryWrapper biomes = LazyBiomeRegistryWrapper.get(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));
        Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noiseRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        return ConfigHandler.Dimensions.Overworld.Default ? WorldGenSettings.makeDefaultOverworld(dynamicRegistries, seed)
                : overworldChunkGenerator(noiseRegistry, biomes, dimensionSettingsRegistry, seed);
    }

    public static ChunkGenerator overworldChunkGenerator(@Nonnull Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource overworld = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry);
        BiomeSource provider = new SkyblockMultiNoiseBiomeSource(biomeRegistry, overworld.parameters, WorldUtil.isSingleBiomeLevel(Level.OVERWORLD));
        NoiseGeneratorSettings settings = dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
        RandomUtility.modifyStructureSettings(settings.structureSettings);

        return new SkyblockNoiseBasedChunkGenerator(noises, provider, seed, () -> settings, Level.OVERWORLD);
    }

    private static ChunkGenerator defaultNetherGenerator(@Nonnull RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        Registry<NormalNoise.NoiseParameters> noiseParameters = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));
        NoiseGeneratorSettings settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
        RandomUtility.modifyStructureSettings(settings.structureSettings);

        return new NoiseBasedChunkGenerator(noiseParameters, biomeSource, seed, () -> settings);
    }

    private static ChunkGenerator netherChunkGenerator(@Nonnull Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource nether = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry);
        BiomeSource provider = new SkyblockMultiNoiseBiomeSource(biomeRegistry, nether.parameters, WorldUtil.isSingleBiomeLevel(Level.NETHER));

        NoiseGeneratorSettings settings = dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER);

        return new SkyblockNoiseBasedChunkGenerator(noises, provider, seed, () -> settings, Level.NETHER);
    }

    private static ChunkGenerator defaultEndGenerator(@Nonnull RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        Registry<NormalNoise.NoiseParameters> noiseParameters = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        TheEndBiomeSource biomeSource = new TheEndBiomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY), seed);
        NoiseGeneratorSettings settings = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);
        RandomUtility.modifyStructureSettings(settings.structureSettings);

        return new NoiseBasedChunkGenerator(noiseParameters, biomeSource, seed, () -> settings);
    }

    private static ChunkGenerator endChunkGenerator(@Nonnull Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        SkyblockEndBiomeSource provider = new SkyblockEndBiomeSource(new TheEndBiomeSource(biomeRegistry, seed));

        NoiseGeneratorSettings settings = dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(noises, provider, seed, () -> settings);
    }
}
