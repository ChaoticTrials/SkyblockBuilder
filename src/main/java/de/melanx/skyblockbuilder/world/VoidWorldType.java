package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Lifecycle;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.BiomeSourceConverter;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
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
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        MappedRegistry<LevelStem> levelStems = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        Registry<DimensionType> dimensionTypes = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> biomes = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);

        levelStems.register(LevelStem.OVERWORLD, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION),
                configuredOverworldChunkGenerator(dynamicRegistries, seed)), Lifecycle.stable());

        levelStems.register(LevelStem.NETHER, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.NETHER_LOCATION),
                ConfigHandler.Dimensions.Nether.Default ? VoidWorldType.defaultNetherGenerator(dynamicRegistries, seed)
                        : netherChunkGenerator(dynamicRegistries, structureSets, noises, biomes, noiseGeneratorSettings, seed)), Lifecycle.stable());

        levelStems.register(LevelStem.END, new LevelStem(dimensionTypes.getOrCreateHolder(DimensionType.END_LOCATION),
                ConfigHandler.Dimensions.End.Default ? VoidWorldType.defaultEndGenerator(dynamicRegistries, seed)
                        : endChunkGenerator(dynamicRegistries, structureSets, noises, biomes, noiseGeneratorSettings, seed)), Lifecycle.stable());

        return levelStems;
    }

    public static ChunkGenerator configuredOverworldChunkGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        Registry<Biome> biomes = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

        return ConfigHandler.Dimensions.Overworld.Default ? WorldGenSettings.makeDefaultOverworld(dynamicRegistries, seed)
                : overworldChunkGenerator(dynamicRegistries, structureSets, noises, biomes, noiseGeneratorSettings, seed);
    }

    public static ChunkGenerator overworldChunkGenerator(RegistryAccess dynamicRegistries, Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, @Nonnull Registry<Biome> biomeRegistry, @Nonnull Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        MultiNoiseBiomeSource biomeSource = (MultiNoiseBiomeSource) BiomeSourceConverter.customBiomeSource(Level.OVERWORLD, dynamicRegistries, MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry, false));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.OVERWORLD);

        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings, Level.OVERWORLD);
    }

    private static ChunkGenerator defaultNetherGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.Preset.NETHER.biomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY));
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.NETHER);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings);
    }

    private static ChunkGenerator netherChunkGenerator(RegistryAccess dynamicRegistries, Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        BiomeSource biomeSource = BiomeSourceConverter.customBiomeSource(Level.NETHER, dynamicRegistries, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.NETHER);

        return new SkyblockNoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings, Level.NETHER);
    }

    private static ChunkGenerator defaultEndGenerator(RegistryAccess dynamicRegistries, long seed) {
        Registry<NoiseGeneratorSettings> noiseGeneratorSettings = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<StructureSet> structureSets = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noises = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);

        TheEndBiomeSource biomeSource = new TheEndBiomeSource(dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY), seed);
        Holder<NoiseGeneratorSettings> settings = noiseGeneratorSettings.getOrCreateHolder(NoiseGeneratorSettings.END);

        return new NoiseBasedChunkGenerator(structureSets, noises, biomeSource, seed, settings);
    }

    private static ChunkGenerator endChunkGenerator(RegistryAccess dynamicRegistries, Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed) {
        TheEndBiomeSource biomeSource = (TheEndBiomeSource) BiomeSourceConverter.customBiomeSource(Level.END, dynamicRegistries, new TheEndBiomeSource(biomeRegistry, seed));
        Holder<NoiseGeneratorSettings> settings = dimensionSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.END);

        return new SkyblockEndChunkGenerator(structureSets, noises, biomeSource, seed, settings, Level.END);
    }
}
