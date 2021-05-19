package de.melanx.skyblockbuilder.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

// TODO 1.17: remove
public class ConfigHandler {

    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final List<Pair<EquipmentSlotType, ItemStack>> STARTER_ITEMS = new ArrayList<>();

    static {
        init(COMMON_BUILDER);
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> whitelistStructures;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> whitelistFeatures;
    public static ForgeConfigSpec.BooleanValue toggleWhitelist;

    public static ForgeConfigSpec.BooleanValue defaultNether;
    public static ForgeConfigSpec.BooleanValue defaultEnd;
    public static ForgeConfigSpec.BooleanValue defaultEndIsland;

    public static ForgeConfigSpec.BooleanValue generateSurface;
    public static ForgeConfigSpec.ConfigValue<String> generationSettings;
    public static ForgeConfigSpec.BooleanValue singleBiome;
    public static ForgeConfigSpec.ConfigValue<String> biome;
    public static ForgeConfigSpec.IntValue seaHeight;
    public static ForgeConfigSpec.IntValue islandDistance;
    public static ForgeConfigSpec.IntValue biomeRange;

    public static ForgeConfigSpec.EnumValue<WorldUtil.Directions> direction;
    public static ForgeConfigSpec.IntValue generationHeight;
    public static ForgeConfigSpec.IntValue spawnRadius;
    public static ForgeConfigSpec.ConfigValue<String> spawnDimension;

    public static ForgeConfigSpec.BooleanValue clearInv;
    public static ForgeConfigSpec.BooleanValue dropItems;

    public static ForgeConfigSpec.BooleanValue selfManageTeam;
    public static ForgeConfigSpec.BooleanValue createOwnTeam;
    public static ForgeConfigSpec.BooleanValue modifySpawns;
    public static ForgeConfigSpec.IntValue modifySpawnRange;
    public static ForgeConfigSpec.BooleanValue homeEnabled;
    public static ForgeConfigSpec.BooleanValue allowVisits;
    public static ForgeConfigSpec.BooleanValue spawnTeleport;

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.push("structures").comment("With this you can configure the structures and features which are generated.",
                "INFO: You can also just use the modid as wildcard for all features/structures from this mod.",
                "WARNING: Some features like trees need special surface!",
                "WARNING: Some structures like mansions only exist in special biomes! If the biome range is too low, the \"/locate\" command will run for a lot of minutes where you cannot play because it blocks the whole server tick.",
                "WARNING: This only works for vanilla dimensions (Overworld, Nether, End)");
        whitelistStructures = builder.comment("All the structures that should be generated.",
                "A list with all possible structures can be found in config/skyblockbuilder/structures.txt")
                .defineList("structures", Collections.singletonList(
                        "minecraft:fortress"
                ), (obj) -> obj instanceof String);
        whitelistFeatures = builder.comment("All the features that should be generated.",
                "A list with all possible structures can be found in config/skyblockbuilder/features.txt",
                "INFO: The two default values are required for the obsidian towers in end. If this is missing, they will be first generated when respawning the dragon.")
                .defineList("features", Arrays.asList(
                        "minecraft:end_spike",
                        "minecraft:end_gateway"
                ), (obj) -> obj instanceof String);
        toggleWhitelist = builder.comment("If this is true, the structure and feature whitelist will be blacklists and everything except of the given structures/features are being generated. [default: false]")
                .define("whitelist-is-blacklist", false);
        builder.pop();

        defaultNether = builder.comment("Should nether generate as in default world type? [default: false]")
                .define("dimensions.nether.default", false);
        defaultEnd = builder.comment("Should end generate as in default world type? [default: false]")
                .define("dimensions.end.default", false);
        defaultEndIsland = builder.comment("Should the main island be generated as normal? [default: true]")
                .define("dimensions.end.main-island", true);

        generateSurface = builder.comment("Should a surface be generated in overworld? [default: false]")
                .define("world.surface", false);
        generationSettings = builder.comment("The block settings for generating the surface.",
                "Same format as flat world generation settings (blocks only)")
                .define("world.surface-settings", "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block", String.class::isInstance);
        seaHeight = builder.comment("Sea level in world [default: 63]")
                .defineInRange("world.sea-level", 63, 0, 256);
        singleBiome = builder.comment("Should only one biome be generated? [default: false]",
                "WARNING: Some structures need a special biome, e.g. Mansion needs Dark Oak Forest! These structures will not be generated if you have only one biome!")
                .define("world.single-biome.enabled", false);
        biome = builder.comment("Specifies the biome for the whole world")
                .define("world.single-biome.biome", "minecraft:plains", String.class::isInstance);
        islandDistance = builder.comment("Distance between islands in overworld [default: 8192]", "nether the distance is 1/8")
                .defineInRange("world.island-distance", 8192, 64, 29999900);
        biomeRange = builder.comment("The radius for the biomes to repeat [default: 8192]", "By default it's the perfect range that each team has the same biomes",
                "WARNING: Too small biome range will prevent some structures to generate, if structures are enabled, because some need a special biome!")
                .defineInRange("world.biome-range", 8192, 64, 29999900);

        direction = builder.comment("Direction the player should look at initial spawn")
                .defineEnum("spawn.direction", WorldUtil.Directions.SOUTH);
        generationHeight = builder.comment("Height of the bottom layer from the structure.",
                "This affects where exactly the island will be generated.")
                .defineInRange("spawn.height", 64, 1, 255);
        spawnRadius = builder.comment("The radius to find a valid spawn if no given spawn is valid")
                .defineInRange("spawn.radius", 50, 0, Integer.MAX_VALUE);
        spawnDimension = builder.comment("The dimension the islands will be generated in. Vanilla dimensions:",
                "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end")
                .define("spawn.dimension", "minecraft:overworld", String.class::isInstance);

        clearInv = builder.comment("Should all items be reset on first world join? [default: false]",
                "This will delete all the items given on spawn from other mods guide books.")
                .define("inventory.clear", false);
        dropItems = builder.comment("Should players' items be dropped when leaving a team? [default: true]")
                .define("inventory.drop", true);

        selfManageTeam = builder.comment("Should players be able to leave their team or invite others? [default: true]")
                .define("utility.self-manage", true);
        createOwnTeam = builder.comment("Should players be able to create their own team? [default: false]")
                .define("utility.create-own-team", false);
        modifySpawns = builder.comment("Should players be able to modify their spawn positions? [default: false]")
                .define("utility.spawns.modify-spawns", false);
        modifySpawnRange = builder.comment("The range from island center for possible spawns to add. [default: 50]")
                .defineInRange("utility.spawns.range", 50, 0, Integer.MAX_VALUE);
        homeEnabled = builder.comment("Should players be able to teleport to their home island? [default: true]")
                .define("utility.teleports.home", true);
        allowVisits = builder.comment("Should players be able to visit other island? [default: true]")
                .define("utility.teleports.allow-visits", true);
        spawnTeleport = builder.comment("Should players be able to teleport to spawn? [default: true]")
                .define("utility.teleports.spawn", true);
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        SkyblockBuilder.getInstance().logger.debug("Loading config file {}", path);
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

    public static void loadStarterItems() throws IOException {
        ConfigHandler.STARTER_ITEMS.clear();

        File spawns = new File(SkyPaths.ITEMS_FILE.toUri());

        String s = IOUtils.toString(new InputStreamReader(new FileInputStream(spawns)));
        JsonObject json = JSONUtils.fromJson(s);

        if (json.has("items")) {
            JsonArray items = json.getAsJsonArray("items");
            Set<EquipmentSlotType> usedTypes = new HashSet<>();
            int slotsUsedInMainInventory = 0;
            for (JsonElement item : items) {
                ItemStack stack = CraftingHelper.getItemStack((JsonObject) item, true);
                EquipmentSlotType slot = ((JsonObject) item).has("Slot") ? EquipmentSlotType.fromString(JSONUtils.getString(item, "Slot")) : EquipmentSlotType.MAINHAND;
                if (slot == EquipmentSlotType.MAINHAND) {
                    if (slotsUsedInMainInventory >= 36) {
                        throw new IllegalStateException("Too many starting items in main inventory. Not more than 36 are allowed.");
                    } else {
                        slotsUsedInMainInventory += 1;
                    }
                } else {
                    if (usedTypes.contains(slot)) {
                        throw new IllegalStateException("Slot type that is not 'mainhand' was used multiple times for starting inventory.");
                    } else {
                        usedTypes.add(slot);
                    }
                }
                ConfigHandler.STARTER_ITEMS.add(Pair.of(slot, stack));
            }
        }
    }

    public static List<Pair<EquipmentSlotType, ItemStack>> getStarterItems() {
        return STARTER_ITEMS;
    }
}
