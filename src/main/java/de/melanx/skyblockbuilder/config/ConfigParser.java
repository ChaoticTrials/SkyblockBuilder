package de.melanx.skyblockbuilder.config;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyPaths;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;

// remove 1.17
public class ConfigParser {

    public static void checkConfig() {
        if (!LibXConfigHandler._reminder) {
            return;
        }

        if (!SkyblockBuilder.oldConfigExists()) {
            LibXConfigHandler._reminder = false;
        } else {
            applyOldConfig();
        }
        deleteOldConfigFile();
    }

    public static void deleteOldConfigFile() {
        try {
            Files.deleteIfExists(SkyPaths.MOD_CONFIG.resolve("config.toml"));
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("config/skyblockbuilder/config.toml could not be deleted");
        }
    }

    public static void applyOldConfig() {
        LibXConfigHandler.Structures.generationStructures = new ResourceList(!ConfigHandler.toggleWhitelist.get(), b -> {
            for (String s : ConfigHandler.whitelistStructures.get()) {
                b.simple(new ResourceLocation(s));
            }
        });
        LibXConfigHandler.Structures.generationFeatures = new ResourceList(!ConfigHandler.toggleWhitelist.get(), b -> {
            for (String s : ConfigHandler.whitelistFeatures.get()) {
                b.simple(new ResourceLocation(s));
            }
        });

        LibXConfigHandler.Dimensions.Nether.Default = ConfigHandler.defaultNether.get();
        LibXConfigHandler.Dimensions.End.Default = ConfigHandler.defaultEnd.get();
        LibXConfigHandler.Dimensions.End.mainIsland = ConfigHandler.defaultEndIsland.get();

        LibXConfigHandler.World.surface = ConfigHandler.generateSurface.get();
        LibXConfigHandler.World.surfaceSettings = ConfigHandler.generationSettings.get();
        LibXConfigHandler.World.seaHeight = ConfigHandler.seaHeight.get();
        LibXConfigHandler.World.islandDistance = ConfigHandler.islandDistance.get();
        LibXConfigHandler.World.biomeRange = ConfigHandler.biomeRange.get();
        LibXConfigHandler.World.SingleBiome.biome = new ResourceLocation(ConfigHandler.biome.get());
        LibXConfigHandler.World.SingleBiome.enabled = ConfigHandler.singleBiome.get();

        LibXConfigHandler.Spawn.radius = ConfigHandler.spawnRadius.get();
        LibXConfigHandler.Spawn.dimension = new ResourceLocation(ConfigHandler.spawnDimension.get());
        LibXConfigHandler.Spawn.direction = ConfigHandler.direction.get();
        LibXConfigHandler.Spawn.height = ConfigHandler.generationHeight.get();

        LibXConfigHandler.Inventory.clearInv = ConfigHandler.clearInv.get();
        LibXConfigHandler.Inventory.dropItems = ConfigHandler.dropItems.get();

        LibXConfigHandler.Utility.selfManage = ConfigHandler.selfManageTeam.get();
        LibXConfigHandler.Utility.createOwnTeam = ConfigHandler.createOwnTeam.get();
        LibXConfigHandler.Utility.Teleports.spawn = ConfigHandler.spawnTeleport.get();
        LibXConfigHandler.Utility.Teleports.allowVisits = ConfigHandler.allowVisits.get();
        LibXConfigHandler.Utility.Teleports.home = ConfigHandler.homeEnabled.get();
        LibXConfigHandler.Utility.Spawns.range = ConfigHandler.modifySpawnRange.get();
        LibXConfigHandler.Utility.Spawns.modifySpawns = ConfigHandler.modifySpawns.get();
    }

}
