package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.StartingInventory;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkyPaths {

    // paths
    public static final Path MOD_CONFIG = FMLPaths.CONFIGDIR.get().resolve("skyblockbuilder");
    public static final Path SKYBLOCK_UTILS = FMLPaths.GAMEDIR.get().resolve("skyblockbuilder");
    public static final Path MOD_EXPORTS = SKYBLOCK_UTILS.resolve("exports");
    public static final Path CONVERT_INPUT = SKYBLOCK_UTILS.resolve("convert_input");
    public static final Path CONVERT_OUTPUT = SKYBLOCK_UTILS.resolve("convert_output");
    public static final Path DUMPS = SKYBLOCK_UTILS.resolve("dumps");
    public static final Path TEMPLATES_DIR = MOD_CONFIG.resolve("templates");
    public static final Path SPREADS_DIR = TEMPLATES_DIR.resolve("spreads");
    public static final Path ICONS_DIR = TEMPLATES_DIR.resolve("icons");
    public static final Path DATA_DIR = MOD_CONFIG.resolve("data");

    // files
    public static final Path ITEMS_FILE = MOD_CONFIG.resolve("starter_inventory.json5");
    public static final Path SCHEMATIC_FILE = TEMPLATES_DIR.resolve("default.nbt");
    private static final Path FEATURES_FILE = DATA_DIR.resolve("features.txt");
    private static final Path STRUCTURES_FILE = DATA_DIR.resolve("structures.txt");
    private static final Path BIOMES_FILE = DATA_DIR.resolve("biomes.txt");
    private static final Path CARVERS_FILE = DATA_DIR.resolve("carvers.txt");
    private static final Path DIMENSIONS_FILE = DATA_DIR.resolve("dimensions.txt");

    public static final Predicate<File> NBT_OR_SNBT = file -> file.isFile() && (file.getName().endsWith(".nbt") || file.getName().endsWith(".snbt"));

    public static void createDirectories() {
        try {
            Files.createDirectories(MOD_CONFIG);
            Files.createDirectories(SKYBLOCK_UTILS);
            Files.createDirectories(MOD_EXPORTS);
            Files.createDirectories(CONVERT_INPUT);
            Files.createDirectories(CONVERT_OUTPUT);
            Files.createDirectories(DUMPS);
            Files.createDirectories(TEMPLATES_DIR);
            Files.createDirectories(SPREADS_DIR);
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
            if (server != null) {
                generateFeatureInformation(server);
                generateStructureInformation(server);
                generateBiomeInformation(server);
                generateCarversInformation(server);
                generateDimensionInformation(server);
            }

            StartingInventory.loadStarterItems();
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("Unable to generate default files", e);
        }
    }

    public static void copyTemplateFile() throws IOException {
        //noinspection ConstantConditions
        if (Arrays.stream(TEMPLATES_DIR.toFile().listFiles()).anyMatch(NBT_OR_SNBT)) {
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
        w.write("// See here for more information:\n");
        w.write("// https://wiki.chaotictrials.de/docs/1.20.x/wiki/mods/skyblock-builder/packdev/config/inventory#starting-inventory\n");
        w.write("// If this page isn't available, go to the project page (where you downloaded the file), and click on the wiki\n");
        w.write(SkyblockBuilder.PRETTY_GSON.toJson(object));
        w.close();
    }

    public static void generateFeatureInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(FEATURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<ConfiguredFeature<?, ?>>> stream = server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).holders();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().location().toString())).forEach(holder -> {
            try {
                w.write(holder.key().location() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().location(), e);
            }
        });

        w.close();
    }

    public static void generateStructureInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(STRUCTURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<Structure>> stream = server.registryAccess().registryOrThrow(Registries.STRUCTURE).holders();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().location().toString())).forEach(holder -> {
            try {
                w.write(holder.key().location() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().location(), e);
            }
        });

        w.close();
    }

    public static void generateBiomeInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(BIOMES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<Biome>> stream = server.registryAccess().registryOrThrow(Registries.BIOME).holders();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().location().toString())).forEach(holder -> {
            try {
                w.write(holder.key().location() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().location(), e);
            }
        });

        w.close();
    }

    public static void generateCarversInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(CARVERS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<ConfiguredWorldCarver<?>>> stream = server.registryAccess().registryOrThrow(Registries.CONFIGURED_CARVER).holders();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().location().toString())).forEach(holder -> {
            try {
                w.write(holder.key().location() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().location(), e);
            }
        });

        w.close();
    }

    public static void generateDimensionInformation(@Nonnull MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(DIMENSIONS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (ResourceKey<Level> levelKey : server.levelKeys()) {
            w.write(levelKey.location() + "\n");
        }

        w.close();
    }
}
