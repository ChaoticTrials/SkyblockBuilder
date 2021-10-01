package de.melanx.skyblockbuilder.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterConfig;
import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.config.Group;
import io.github.noeppi_noeppi.libx.config.validator.DoubleRange;
import io.github.noeppi_noeppi.libx.config.validator.IntRange;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RegisterConfig("common-config")
public class ConfigHandler {

    private static final List<Pair<EquipmentSlot, ItemStack>> STARTER_ITEMS = new ArrayList<>();

    @Group(value = {"With this you can configure the structures and features which are generated.",
            "INFO: You can also just use the modid as wildcard for all features/structures from this mod.",
            "WARNING: Some features like trees need special surface!",
            "WARNING: Some structures like mansions only exist in special biomes! If the biome range is too low, the \"/locate\" command will run for a lot of minutes where you cannot play because it blocks the whole server tick.",
            "WARNING: This only works for vanilla dimensions (Overworld, Nether, End)"})
    public static class Structures {

        @Config({"All the structures that should be generated.", "A list with all possible structures can be found in config/skyblockbuilder/data/structures.txt"})
        public static ResourceList generationStructures = new ResourceList(true, b -> {
            b.simple(new ResourceLocation("minecraft", "fortress"));
        });

        @Config({"All the features that should be generated.", "A list with all possible structures can be found in config/skyblockbuilder/data/features.txt",
                "INFO: The two default values are required for the obsidian towers in end. If this is missing, they will be first generated when respawning the dragon."})
        public static ResourceList generationFeatures = new ResourceList(true, b -> {
            b.simple(new ResourceLocation("minecraft", "end_spike"));
            b.simple(new ResourceLocation("minecraft", "end_gateway"));
        });
    }

    public static class Dimensions {

        public static class Overworld {

            @Config("Should overworld generate as in default world type? [default: false]")
            public static boolean Default = false;
        }

        public static class Nether {

            @Config("Should nether generate as in default world type? [default: false]")
            public static boolean Default = false;
        }

        public static class End {

            @Config("Should end generate as in default world type? [default: false]")
            public static boolean Default = false;

            @Config("Should the main island be generated as normal? [default: true]")
            public static boolean mainIsland = true;
        }
    }

    public static class World {

        @Config("Should a surface be generated in the dimensions? [default: false]")
        public static boolean surface = false;

        @Config({"The block settings for generating the different dimensions surfaces.", "Same format as flat world generation settings (blocks only)"})
        public static Map<String, String> surfaceSettings = initSurfaceSettingsMap(Maps.newHashMap());

        @Config("Sea level in world [default: 63]")
        @IntRange(min = 0, max = 256)
        public static int seaHeight = 63;

        @Config({"Distance between islands in overworld [default: 8192]", "nether the distance is 1/8"})
        @IntRange(min = 64, max = 29999900)
        public static int islandDistance = 8192;

        @Config({"The radius for the biomes to repeat [default: 8192]", "By default it's the perfect range that each team has the same biomes",
                "WARNING: Too small biome range will prevent some structures to generate, if structures are enabled, because some need a special biome!"})
        @IntRange(min = 64, max = 29999900)
        public static int biomeRange = 8192;

        @Config({"The offset from 0, 0 to generate the islands", "Can be used to generate them in the middle of .mca files"})
        public static int offset = 0;

        @Config({"The modifier for spacing and separation of structures. These values can be defined by a data pack. However, this is a multiplier to change these values.",
                "Minimal spacing is 1", "Minimal separation is 0"})
        @DoubleRange(min = 0, max = 1000)
        public static double structureModifier = 1;

        private static Map<String, String> initSurfaceSettingsMap(Map<String, String> map) {
            map.put(Level.OVERWORLD.location().toString(), "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block");
            map.put(Level.NETHER.location().toString(), "");
            map.put(Level.END.location().toString(), "");
            return map;
        }

        public static class SingleBiome {

            @Config({"Specifies the biome for the whole world", "A list with all possible structures can be found in config/skyblockbuilder/data/biomes.txt"})
            public static ResourceLocation biome = new ResourceLocation("minecraft", "plains");

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @Config("The dimension where the single biome should be applied. Use \"null\" for spawn dimension")
            public static Optional<WorldUtil.Dimension> singleBiomeDimension = Optional.empty();

            @Config({"Should only one biome be generated? [default: false]",
                    "WARNING: Some structures need a special biome, e.g. Mansion needs Dark Oak Forest! These structures will not be generated if you have only one biome!"})
            public static boolean enabled = false;
        }
    }

    public static class Spawn {

        @Config("The radius to find a valid spawn if no given spawn is valid")
        @IntRange(min = 0)
        public static int radius = 50;

        @Config({"The dimension the islands will be generated in."})
        public static WorldUtil.Dimension dimension = WorldUtil.Dimension.OVERWORLD;

        @Config("Direction the player should look at initial spawn")
        public static WorldUtil.Directions direction = WorldUtil.Directions.SOUTH;

        @Config({"Height of the bottom layer from the structure.", "This affects where exactly the island will be generated."})
        @IntRange(min = 0, max = 255)
        public static int height = 64;
    }

    public static class Inventory {

        @Config({"Should all items be reset on first world join? [default: false]",
                "This will delete all the items given on spawn from other mods guide books."})
        public static boolean clearInv = false;

        @Config("Should players' items be dropped when leaving a team? [default: true]")
        public static boolean dropItems = true;
    }

    public static class Utility {

        @Config("Should players be able to leave their team or invite others? [default: true]")
        public static boolean selfManage = true;

        @Config("Should players be able to create their own team? [default: false]")
        public static boolean createOwnTeam = false;

        public static class Teleports {

            @Config("Should players be able to teleport to spawn? [default: true]")
            public static boolean spawn = true;

            @Config("Should players be able to visit other island? [default: true]")
            public static boolean allowVisits = true;

            @Config("Should players be able to teleport to their home island? [default: true]")
            public static boolean home = true;
        }

        public static class Spawns {

            @Config("The range from island center for possible spawns to add. [default: 50]")
            public static int range = 50;

            @Config("Should players be able to modify their spawn positions? [default: false]")
            public static boolean modifySpawns = false;
        }
    }

    public static void loadStarterItems() throws IOException {
        ConfigHandler.STARTER_ITEMS.clear();

        File spawns = new File(SkyPaths.ITEMS_FILE.toUri());

        String s = IOUtils.toString(new InputStreamReader(new FileInputStream(spawns)));
        JsonObject json = GsonHelper.parse(s);

        if (json.has("items")) {
            JsonArray items = json.getAsJsonArray("items");
            Set<EquipmentSlot> usedTypes = new HashSet<>();
            int slotsUsedInMainInventory = 0;
            for (JsonElement item : items) {
                JsonObject itemObj = (JsonObject) item;
                ItemStack stack = CraftingHelper.getItemStack(itemObj, true);
                EquipmentSlot slot = (itemObj).has("Slot") ? EquipmentSlot.byName(GsonHelper.getAsString(itemObj, "Slot")) : EquipmentSlot.MAINHAND;
                if (slot == EquipmentSlot.MAINHAND) {
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

    public static List<Pair<EquipmentSlot, ItemStack>> getStarterItems() {
        return ConfigHandler.STARTER_ITEMS;
    }
}
