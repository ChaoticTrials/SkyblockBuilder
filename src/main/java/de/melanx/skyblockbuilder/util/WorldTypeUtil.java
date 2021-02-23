package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.ServerWorldInfo;

import java.util.Locale;
import java.util.Optional;

public class WorldTypeUtil {
    private static boolean isServerLevelSkyblock(DedicatedServer server) {
        String levelType = Optional.ofNullable((String) server.getServerProperties().serverProperties.get("level-type")).map(str -> str.toLowerCase(Locale.ROOT)).orElse("default");
        return levelType.equals("custom_skyblock");
    }

    public static void setupForDedicatedServer(DedicatedServer server) {
        if (!isServerLevelSkyblock(server)) {
            return;
        }

        DynamicRegistries registries = server.func_244267_aX();
        ServerWorldInfo worldInfo = (ServerWorldInfo) server.getServerConfiguration().getServerWorldInfo();
        long seed = worldInfo.generatorSettings.getSeed();
        Registry<DimensionType> dimensions = registries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        Registry<Biome> biomes = registries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> dimensionSettings = registries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        SimpleRegistry<Dimension> skyblock = DimensionGeneratorSettings.func_242749_a(dimensions, VoidWorldType.voidDimensions(registries, biomes, dimensionSettings, seed), new SkyblockOverworldChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomes), seed, () -> dimensionSettings.getOrThrow(DimensionSettings.field_242734_c)));
        worldInfo.generatorSettings = new DimensionGeneratorSettings(seed, worldInfo.generatorSettings.doesGenerateFeatures(), worldInfo.generatorSettings.hasBonusChest(), skyblock);
    }
}
