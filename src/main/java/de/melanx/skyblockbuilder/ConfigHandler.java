package de.melanx.skyblockbuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigHandler {
    private static final Path MOD_CONFIG = FMLPaths.CONFIGDIR.get().resolve(SkyblockBuilder.MODID);
    private static final Path SCHEMATIC_FILE = MOD_CONFIG.resolve("template.nbt");
    private static final Path SPAWNS_FILE = MOD_CONFIG.resolve("spawns.json");

    public static void generateDefaultFiles() {
        try {
            if (!Files.isDirectory(MOD_CONFIG)) {
                Files.createDirectories(MOD_CONFIG);
            }

            copyTemplateFile();
            generateSpawnsFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyTemplateFile() throws IOException {
        if (Files.isRegularFile(SCHEMATIC_FILE)) {
            return;
        }

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

}
