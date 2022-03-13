package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.LazyBiomeRegistryWrapper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockMultiNoiseBiomeSource;
import de.melanx.skyblockbuilder.world.dimensions.multinoise.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.levelgen.structure.StructureSet;
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
        return VoidWorldType.configuredOverworldChunkGenerator(dynamicRegistries, seed);
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);

        Registry<LevelStem> dimensions = WorldGenSettings.withOverworld(
                dimensionTypeRegistry,
                voidDimensions(dynamicRegistries, seed),
                this.createChunkGenerator(dynamicRegistries, seed, null)
        );

        return new WorldGenSettings(seed, generateStructures, generateLoot, dimensions);
    }

    public static MappedRegistry<LevelStem> voidDimensions(RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noiseRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        MappedRegistry<LevelStem> registry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        Registry<DimensionType> dimensionTypes = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        LazyBiomeRegistryWrapper biomes = LazyBiomeRegistryWrapper.get(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));

        registry.register(LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION),
                configuredOverworldChunkGenerator(dynamicRegistries, seed)), Lifecycle.stable());

        registry.register(LevelStem.NETHER, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.NETHER_LOCATION),
                ConfigHandler.Dimensions.Nether.Default ? VoidWorldType.defaultNetherGenerator(dynamicRegistries, seed)
                        : netherChunkGenerator(structureSets, noiseRegistry, biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());

        registry.register(LevelStem.END, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.END_LOCATION),
                ConfigHandler.Dimensions.End.Default ? VoidWorldType.defaultEndGenerator(dynamicRegistries, seed)
                        : endChunkGenerator(structureSets, noiseRegistry, biomes, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        return registry;
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noiseRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        LazyBiomeRegistryWrapper biomes = LazyBiomeRegistryWrapper.get(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));
        Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        return ConfigHandler.Dimensions.Overworld.Default ? WorldGenSettings.makeDefaultOverworld(dynamicRegistries, seed)
                : overworldChunkGenerator(structureSets, noiseRegistry, biomes, dimensionSettingsRegistry, seed);
    }

    public static ChunkGenerator overworldChunkGenerator(@Nonnull Registry<StructureSet> structureSets, @Nonnull Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource overworld = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry);
        BiomeSource provider = new SkyblockMultiNoiseBiomeSource(biomeRegistry, overworld.parameters, WorldUtil.isSingleBiomeLevel(Level.OVERWORLD));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD);
//        RandomUtility.modifyStructureSettings(settings.structureSettings); TODO?

        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, provider, seed, settings, Level.OVERWORLD);
    }

    private static ChunkGenerator defaultNetherGenerator(@Nonnull RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.NETHER);
//        RandomUtility.modifyStructureSettings(settings.structureSettings);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings);
    }

    private static ChunkGenerator netherChunkGenerator(@Nonnull Registry<StructureSet> structureSets, @Nonnull Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource nether = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry);
        BiomeSource provider = new SkyblockMultiNoiseBiomeSource(biomeRegistry, nether.parameters, WorldUtil.isSingleBiomeLevel(Level.NETHER));

        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.NETHER);

        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, provider, seed, settings, Level.NETHER);
    }

    private static ChunkGenerator defaultEndGenerator(@Nonnull RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        TheEndBiomeSource biomeSource = new TheEndBiomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY), seed);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.END);
//        RandomUtility.modifyStructureSettings(settings.structureSettings);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings);
    }

    private static ChunkGenerator endChunkGenerator(@Nonnull Registry<StructureSet> structureSets, @Nonnull Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        SkyblockEndBiomeSource provider = new SkyblockEndBiomeSource(biomeRegistry, new TheEndBiomeSource(biomeRegistry, seed));
//        SkyblockMultiNoiseBiomeSource provider = new SkyblockMultiNoiseBiomeSource(biomeRegistry, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry).parameters);

        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(structureSets, noises, provider, seed, settings, Level.END);
    }
}
