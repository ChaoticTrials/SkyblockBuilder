package de.melanx.skyblockbuilder.config;

import com.google.common.collect.Maps;
import de.melanx.skyblockbuilder.SpawnProtectionEvents;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.Group;
import org.moddingx.libx.config.validate.IntRange;
import org.moddingx.libx.util.data.ResourceList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RegisterConfig("common-config")
public class ConfigHandler {

    @Group(value = {"With this you can configure the structures and features which are generated.",
            "INFO: You can also just use the modid as wildcard for all features/structures from this mod.",
            "WARNING: Some features like trees need special surface!",
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
            b.simple(new ResourceLocation("minecraft", "end_gateway_return"));
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

        @Config({"A list of biomes for each dimension.",
                "You can not use this for the end dimension. The end dimension will always have it's five biomes.",
                "Overworld has all oceans by default because animals cannot spawn in these biomes.",
                "These are resource lists. See https://moddingx.org/libx/org/moddingx/libx/util/data/ResourceList.html#use_resource_lists_in_configs"})
        public static Map<String, ResourceList> biomes = Util.make(Maps.newHashMap(), map -> {
            map.put(Level.OVERWORLD.location().toString(), new ResourceList(false, b -> b.parse("minecraft:*ocean*")));
            map.put(Level.NETHER.location().toString(), ResourceList.DENY_LIST);
        });

        @Config("Should a surface be generated in the dimensions? [default: false]")
        public static boolean surface = false;

        @Config({"The block settings for generating the different dimensions surfaces.", "Same format as flat world generation settings (blocks only)",
                "WARNING: Does not work with modded blocks, see https://github.com/MelanX/SkyblockBuilder/issues/133#issuecomment-2075351219"})
        public static Map<String, String> surfaceSettings = Util.make(Maps.newHashMap(), map -> {
            map.put(Level.OVERWORLD.location().toString(), "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block");
            map.put(Level.NETHER.location().toString(), "");
            map.put(Level.END.location().toString(), "");
        });

        @Config
        public static Map<String, ResourceList> carvers = Util.make(Maps.newHashMap(), map -> {
            map.put(Level.OVERWORLD.location().toString(), ResourceList.ALLOW_LIST);
            map.put(Level.NETHER.location().toString(), ResourceList.ALLOW_LIST);
            map.put(Level.END.location().toString(), ResourceList.ALLOW_LIST);
        });

        @Config("Sea level in world [default: 63]")
        public static int seaHeight = 63;

        @Config({"Distance between islands in overworld [default: 8192]", "nether the distance is 1/8"})
        @IntRange(min = 64, max = 29999900)
        public static int islandDistance = 8192;

        @Config({"The offset from 0, 0 to generate the islands", "Can be used to generate them in the middle of .mca files"})
        public static int offset = 0;
    }

    public static class Spawn {

        @Config("The entities which you can interact with within the spawn protection")
        public static ResourceList interactionEntitiesInSpawnProtection = ResourceList.ALLOW_LIST;

        @Config("The radius of chunks where to apply spawn protection. In this area, only op players can avoid this.")
        public static int spawnProtectionRadius = 0;

        @Config({"A list of event types which will be prevented:",
                "   interact_entities = Interacting with entities, e.g. riding a pig",
                "   interact_blocks   = Interacting with blocks, e.g. activating buttons, placing, or destroying blocks",
                "   mob_griefing      = Mobs destroying the world",
                "   explosions        = TNT, creeper, or other explosions",
                "   crop_grow         = Crops increasing their growth status",
                "   mobs_spawn        = Mobs spawning",
                "   mobs_spawn_egg    = Mobs being summoned using a spawn egg",
                "   damage            = Attacking others, or getting attacked",
                "   healing           = Getting healed and saturated on spawn"})
        public static List<SpawnProtectionEvents.Type> spawnProtectionEvents = Util.make(new ArrayList<>(), list -> {
            list.add(SpawnProtectionEvents.Type.INTERACT_ENTITIES);
            list.add(SpawnProtectionEvents.Type.INTERACT_BLOCKS);
            list.add(SpawnProtectionEvents.Type.MOB_GRIEFING);
            list.add(SpawnProtectionEvents.Type.EXPLOSIONS);
            list.add(SpawnProtectionEvents.Type.CROP_GROW);
            list.add(SpawnProtectionEvents.Type.MOBS_SPAWN);
            list.add(SpawnProtectionEvents.Type.MOBS_SPAWN_EGG);
            list.add(SpawnProtectionEvents.Type.DAMAGE);
            list.add(SpawnProtectionEvents.Type.HEALING);
        });

        @Config("The radius to find a valid spawn if no given spawn is valid")
        @IntRange(min = 0)
        public static int radius = 50;

        @Config({"The dimension the islands will be generated in."})
        public static ResourceKey<Level> dimension = Level.OVERWORLD;

        public static class Height {

            @Config({"set:",
                    "   Uses the bottom height of the range",
                    "range_top:",
                    "   Searches from the top position down to the bottom position for a valid spawn.",
                    "   If no valid position was found, the top position will be used.",
                    "range_bottom:",
                    "   Searches from the top position down to the bottom position for a valid spawn.",
                    "   If no valid position was found, the bottom position will be used."})
            public static SpawnSettings.Type spawnType = SpawnSettings.Type.SET;

            @Config({"You can set a range from minY to maxY. minY is the bottom spawn position. maxY is the top spawn dimension.",
                    "If you set the spawn height type to \"set\", the bottom value will be used for a set height. " +
                            "Otherwise, the height will be calculated."})
            public static SpawnSettings.Range range = new SpawnSettings.Range(64, 319);

            @Config({"If the spawn height type is set to \"range\", this offset will be used to slightly move the spawn height in any direction.",
                    "Negative values go down, positive values go up."})
            public static int offset = 0;
        }
    }

    public static class Inventory {

        @Config({"Should all items be reset on first world join? [default: false]",
                "This will delete all the items given on spawn from other mods guide books."})
        public static boolean clearInv = false;

        @Config("Should players' items be dropped when leaving a team? [default: true]")
        public static boolean dropItems = true;
    }

    public static class Utility {

        @Config({"Force the check if the world is skyblock",
                "This enables the commands in worlds without any skyblock dimension",
                "USE AT YOUR OWN RISK, NO SUPPORT FOR DEFAULT WORLDS WITH THIS ENABLED"})
        public static boolean forceSkyblockCheck = false;

        @Config("Should players be able to leave their team or invite others? [default: true]")
        public static boolean selfManage = true;

        @Config("Should players be able to create their own team? [default: false]")
        public static boolean createOwnTeam = false;

        public static class Teleports {

            @Config("Should players be able to teleport to spawn? [default: true]")
            public static boolean spawn = true;

            @Config("Cooldown in ticks for teleporting to spawn. [default: 3600 = 3min]")
            public static int spawnCooldown = 3600;

            @Config("Should players be able to visit other island? [default: true]")
            public static boolean allowVisits = true;

            @Config("Should players be able to teleport to their home island? [default: true]")
            public static boolean home = true;

            @Config("Cooldown in ticks for teleporting back home. [default: 3600 = 3min]")
            public static int homeCooldown = 3600;

            @Config("Should players be able to teleport to another dimension? [default: true]")
            public static boolean crossDimensionTeleportation = true;

            @Config("Dimensions in this list are not allowed for executing teleportation commands. Inverted behaviour if you set \"allow_list\" to true.")
            public static ResourceList teleportationDimensions = ResourceList.DENY_LIST;
        }

        public static class Spawns {

            @Config("The range from island center for possible spawns to add. [default: 50]")
            public static int range = 50;

            @Config("Should players be able to modify their spawn positions? [default: true]")
            public static boolean modifySpawns = true;
        }
    }
}
