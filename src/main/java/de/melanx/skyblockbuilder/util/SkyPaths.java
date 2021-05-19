package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SkyPaths {

    // paths
    public static final Path MOD_CONFIG = FMLPaths.CONFIGDIR.get().resolve("skyblockbuilder");
    public static final Path MOD_EXPORTS = FMLPaths.GAMEDIR.get().resolve("skyblock_exports");
    public static final Path TEMPLATES_DIR = MOD_CONFIG.resolve("templates");

    // files
    public static final Path ITEMS_FILE = MOD_CONFIG.resolve("starter_item.json");
    private static final Path SPAWNS_FILE = MOD_CONFIG.resolve("spawns.json");
    private static final Path SCHEMATIC_FILE = MOD_CONFIG.resolve("template.nbt");
    private static final Path FEATURES_FILE = MOD_CONFIG.resolve("features.txt");
    private static final Path STRUCTURES_FILE = MOD_CONFIG.resolve("structures.txt");

    public static void createDirectories() {
        try {
            Files.createDirectories(MOD_CONFIG);
            Files.createDirectories(MOD_EXPORTS);
            Files.createDirectories(TEMPLATES_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create default directories.", e);
        }
    }

    public static void generateDefaultFiles() {
        try {
            createDirectories();

            copyTemplateFile();
            generateSpawnsFile();
            generateStarterItemsFile();
            generateFeatureInformation();
            generateStructureInformation();

            ConfigHandler.loadStarterItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyTemplateFile() throws IOException {
        if (Files.isRegularFile(SCHEMATIC_FILE)) {
            return;
        }

        //noinspection ConstantConditions
        Files.copy(SkyblockBuilder.class.getResourceAsStream("/skyblockbuilder-template.nbt"), SCHEMATIC_FILE);
    }

    private static void generateSpawnsFile() throws IOException {
        if (Files.isRegularFile(SPAWNS_FILE)) {
            return;
        }

        JsonObject object = new JsonObject();

        JsonArray spawns = new JsonArray();
        JsonArray defaultSpawn = new JsonArray();
        defaultSpawn.add(6);
        defaultSpawn.add(3);
        defaultSpawn.add(5);
        spawns.add(defaultSpawn);

        object.add("islandSpawns", spawns);

        BufferedWriter w = Files.newBufferedWriter(SPAWNS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        w.write(SkyblockBuilder.PRETTY_GSON.toJson(object));
        w.close();
    }

    private static void generateStarterItemsFile() throws IOException {
        if (Files.isRegularFile(ITEMS_FILE)) {
            return;
        }

        JsonObject object = new JsonObject();
        JsonArray items = new JsonArray();
        object.add("items", items);

        BufferedWriter w = Files.newBufferedWriter(ITEMS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        w.write(SkyblockBuilder.PRETTY_GSON.toJson(object));
        w.close();
    }

    public static void generateFeatureInformation() throws IOException {
        BufferedWriter w = Files.newBufferedWriter(FEATURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (Feature<?> feature : ForgeRegistries.FEATURES.getValues()) {
            if (feature.getRegistryName() != null) {
                w.write(feature.getRegistryName().toString() + "\n");
            }
        }

        w.close();
    }

    public static void generateStructureInformation() throws IOException {
        BufferedWriter w = Files.newBufferedWriter(STRUCTURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (Structure<?> feature : ForgeRegistries.STRUCTURE_FEATURES.getValues()) {
            if (feature.getRegistryName() != null) {
                w.write(feature.getRegistryName().toString() + "\n");
            }
        }

        w.close();
    }
}
