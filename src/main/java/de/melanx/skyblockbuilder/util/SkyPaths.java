package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.StartingInventory;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class SkyPaths {

    // paths
    public static final Path MOD_CONFIG = FMLPaths.CONFIGDIR.get().resolve("skyblockbuilder");
    public static final Path MOD_EXPORTS = FMLPaths.GAMEDIR.get().resolve("skyblock_exports");
    public static final Path TEMPLATES_DIR = MOD_CONFIG.resolve("templates");
    public static final Path ICONS_DIR = TEMPLATES_DIR.resolve("icons");
    public static final Path DATA_DIR = MOD_CONFIG.resolve("data");

    // files
    public static final Path ITEMS_FILE = MOD_CONFIG.resolve("starter_item.json");
    public static final Path SCHEMATIC_FILE = TEMPLATES_DIR.resolve("default.nbt");
    private static final Path FEATURES_FILE = DATA_DIR.resolve("features.txt");
    private static final Path STRUCTURES_FILE = DATA_DIR.resolve("structures.txt");
    private static final Path BIOMES_FILE = DATA_DIR.resolve("biomes.txt");
    private static final Path DIMENSIONS_FILE = DATA_DIR.resolve("dimensions.txt");

    public static void createDirectories() {
        try {
            Files.createDirectories(MOD_CONFIG);
            Files.createDirectories(MOD_EXPORTS);
            Files.createDirectories(TEMPLATES_DIR);
            Files.createDirectories(ICONS_DIR);
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create default directories.", e);
        }
    }

    public static void generateDefaultFiles(@Nullable MinecraftServer server) {
        try {
            createDirectories();

            copyTemplateFile();
            generateStarterItemsFile();
            generateFeatureInformation();
            generateStructureInformation();
            generateBiomeInformation();
            if (server != null) {
                generateDimensionInformation(server);
            }

            StartingInventory.loadStarterItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyTemplateFile() throws IOException {
        //noinspection ConstantConditions
        if (Arrays.stream(TEMPLATES_DIR.toFile().listFiles())
                .anyMatch(file -> file.isFile() && file.getName().endsWith(".nbt"))) {
            return;
        }

        //noinspection ConstantConditions
        Files.copy(SkyblockBuilder.class.getResourceAsStream("/skyblockbuilder-template.nbt"), SCHEMATIC_FILE);
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

        for (Holder.Reference<PlacedFeature> feature : BuiltinRegistries.PLACED_FEATURE.holders().toList()) {
            w.write(feature.key().location().toString() + "\n");
        }

        w.close();
    }

    public static void generateStructureInformation() throws IOException {
        BufferedWriter w = Files.newBufferedWriter(STRUCTURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (StructureFeature<?> feature : ForgeRegistries.STRUCTURE_FEATURES.getValues()) {
            if (feature.getRegistryName() != null) {
                w.write(feature.getRegistryName().toString() + "\n");
            }
        }

        w.close();
    }

    public static void generateBiomeInformation() throws IOException {
        BufferedWriter w = Files.newBufferedWriter(BIOMES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
            if (biome.getRegistryName() != null) {
                w.write(biome.getRegistryName().toString() + "\n");
            }
        }

        w.close();
    }

    public static void generateDimensionInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(DIMENSIONS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (ResourceKey<Level> levelKey : server.levelKeys()) {
            w.write(levelKey.location() + "\n");
        }

        w.close();
    }
}
