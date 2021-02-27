package de.melanx.skyblockbuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigHandler {
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final Path MOD_CONFIG = FMLPaths.CONFIGDIR.get().resolve(SkyblockBuilder.MODID);
    private static final Path SCHEMATIC_FILE = MOD_CONFIG.resolve("template.nbt");
    private static final Path SPAWNS_FILE = MOD_CONFIG.resolve("spawns.json");

    static {
        init(COMMON_BUILDER);
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static ForgeConfigSpec.BooleanValue overworldStructures;
    public static ForgeConfigSpec.BooleanValue strongholdOnly;
    public static ForgeConfigSpec.BooleanValue netherStructures;
    public static ForgeConfigSpec.BooleanValue disableFortress;
    public static ForgeConfigSpec.BooleanValue disableBastion;
    public static ForgeConfigSpec.BooleanValue defaultEnd;
    public static ForgeConfigSpec.BooleanValue defaultEndIsland;
    public static ForgeConfigSpec.BooleanValue endStructures;
    public static ForgeConfigSpec.EnumValue<WorldUtil.Directions> direction;
    public static ForgeConfigSpec.IntValue generationHeight;

    public static void init(ForgeConfigSpec.Builder builder) {
        overworldStructures = builder.comment("Should structures like end portal or villages be generated in overworld? [default: false]")
                .define("dimensions.overworld.structures", false);
        strongholdOnly = builder.comment("Should the stronghold with end portal be the only structure?", "Needs default config be 'true', otherwise it'll be ignored. [default: false]")
                .define("dimensions.overworld.stronhold-only", false);

        netherStructures = builder.comment("Should structures like fortresses or bastions be generated in nether? [default: true]")
                .define("dimensions.nether.structures.enabled", true);
        disableFortress = builder.comment("Use only if 'enabled' is true!", "Should nether fortress be disabled? [default: false]")
                .define("dimensions.nether.structures.disable-fortress", false);
        disableBastion = builder.comment("Use only if 'enabled' is true!", "Should bastions be disabled? [default: true]")
                .define("dimensions.nether.structures.disable-bastions", true);

        defaultEnd = builder.comment("Should end generate as in default world type? [default: false]")
                .define("dimensions.end.default", false);
        defaultEndIsland = builder.comment("Should the main island be generated as normal? [default: true]")
                .define("dimensions.end.main-island", true);
        endStructures = builder.comment("Should structures like end cities be generated in nether? [default: false]",
                "This also affects the large islands with chorus plants.", "Small islands will still be generated.")
                .define("dimensions.end.structures", false);

        direction = builder.comment("Direction the player should look at initial spawn")
                .defineEnum("spawn.direction", WorldUtil.Directions.SOUTH);
        generationHeight = builder.comment("Height of the bottom layer from the structure.",
                "This affects where exactly the island will be generated.")
                .defineInRange("spawn.height", 64, 1, 255);
    }

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

    public static void setup() {
        generateDefaultFiles();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG, SkyblockBuilder.MODID + "/config.toml");
    }

}
