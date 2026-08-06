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
import net.neoforged.fml.loading.FMLPaths;

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
    public static final Path ISLANDS_DIR = TEMPLATES_DIR.resolve("islands");
    public static final Path SPREADS_DIR = TEMPLATES_DIR.resolve("spreads");
    public static final Path PORTALS_DIR = TEMPLATES_DIR.resolve("portals");
    public static final Path ICONS_DIR = TEMPLATES_DIR.resolve("icons");
    public static final Path DATA_DIR = MOD_CONFIG.resolve("data");

    // files
    public static final Path ITEMS_FILE = MOD_CONFIG.resolve("starter_inventory.json5");
    public static final Path SCHEMATIC_FILE = ISLANDS_DIR.resolve("default.nbt");
    private static final Path FEATURES_FILE = DATA_DIR.resolve("features.txt");
    private static final Path STRUCTURES_FILE = DATA_DIR.resolve("structures.txt");
    private static final Path BIOMES_FILE = DATA_DIR.resolve("biomes.txt");
    private static final Path CARVERS_FILE = DATA_DIR.resolve("carvers.txt");
    private static final Path DIMENSIONS_FILE = DATA_DIR.resolve("dimensions.txt");
    private static final Path PORTALS_INFORMATION_FILE = PORTALS_DIR.resolve("information.txt");

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
            Files.createDirectories(ISLANDS_DIR);
            Files.createDirectories(SPREADS_DIR);
            Files.createDirectories(PORTALS_DIR);
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
            writePortalsInformation();
            generateStarterItemsFile();
            if (server != null) {
                generateFeatureInformation(server);
                generateStructureInformation(server);
                generateBiomeInformation(server);
                generateCarversInformation(server);
                generateDimensionInformation(server);
                StartingInventory.loadStarterItems(server.registryAccess());
            }
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("Unable to generate default files", e);
        }
    }

    public static void copyTemplateFile() throws IOException {
        //noinspection ConstantConditions
        if (Arrays.stream(ISLANDS_DIR.toFile().listFiles()).anyMatch(NBT_OR_SNBT)) {
            return;
        }

        //noinspection ConstantConditions
        Files.copy(SkyblockBuilder.class.getResourceAsStream("/skyblockbuilder-template.nbt"), SCHEMATIC_FILE);
    }

    public static void writePortalsInformation() throws IOException {
        String fileContent = """
                This directory is only for providing custom portals. At the moment, you only may set a custom portal when entering the nether.
                To do so, call your file "to_nether.nbt" or "to_nether.snbt".
                It needs to contain at least one nether portal block. If that is destroyed, the structure would re-generate when re-entering the nether.
                To place additional structures around the portal on a team's first nether visit, use the "netherSpreads" config in templates.json5.
                Spread files are saved to the spreads directory (not this one) using the structure saver item in spread mode.""";

        if (Files.exists(PORTALS_INFORMATION_FILE) && Files.readString(PORTALS_INFORMATION_FILE).equals(fileContent)) {
            return;
        }

        Files.writeString(PORTALS_INFORMATION_FILE, fileContent);
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
        w.write("// https://wiki.chaotictrials.de/docs/wiki/skyblock-builder/packdev/configs/starting-inventory\n");
        w.write("// If this page isn't available, go to the project page (where you downloaded the file), and click on the wiki\n");
        w.write(SkyblockBuilder.PRETTY_GSON.toJson(object));
        w.close();
    }

    public static void generateFeatureInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(FEATURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<ConfiguredFeature<?, ?>>> stream = server.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).listElements();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().identifier().toString())).forEach(holder -> {
            try {
                w.write(holder.key().identifier() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().identifier(), e);
            }
        });

        w.close();
    }

    public static void generateStructureInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(STRUCTURES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<Structure>> stream = server.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().identifier().toString())).forEach(holder -> {
            try {
                w.write(holder.key().identifier() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().identifier(), e);
            }
        });

        w.close();
    }

    public static void generateBiomeInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(BIOMES_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<Biome>> stream = server.registryAccess().lookupOrThrow(Registries.BIOME).listElements();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().identifier().toString())).forEach(holder -> {
            try {
                w.write(holder.key().identifier() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().identifier(), e);
            }
        });

        w.close();
    }

    public static void generateCarversInformation(MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(CARVERS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Stream<Holder.Reference<ConfiguredWorldCarver<?>>> stream = server.registryAccess().lookupOrThrow(Registries.CONFIGURED_CARVER).listElements();

        //noinspection DuplicatedCode
        stream.sorted(Comparator.comparing(holder -> holder.key().identifier().toString())).forEach(holder -> {
            try {
                w.write(holder.key().identifier() + "\n");
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed to write '{}' to file", holder.key().identifier(), e);
            }
        });

        w.close();
    }

    public static void generateDimensionInformation(@Nonnull MinecraftServer server) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(DIMENSIONS_FILE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        for (ResourceKey<Level> levelKey : server.levelKeys()) {
            w.write(levelKey.identifier() + "\n");
        }

        w.close();
    }
}
