package de.melanx.skyblockbuilder.config;

import com.google.common.collect.Maps;
import de.melanx.skyblockbuilder.util.WorldUtil;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterConfig;
import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.config.Group;
import io.github.noeppi_noeppi.libx.config.validator.DoubleRange;
import io.github.noeppi_noeppi.libx.config.validator.IntRange;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;

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
        public static int seaHeight = 63;

        @Config({"Distance between islands in overworld [default: 8192]", "nether the distance is 1/8"})
        @IntRange(min = 64, max = 29999900)
        public static int islandDistance = 8192;

        @Config({"The offset from 0, 0 to generate the islands", "Can be used to generate them in the middle of .mca files"})
        public static int offset = 0;

        @Config({"The modifier for spacing and separation of structures. These values can be defined by a data pack. However, this is a multiplier to change these values.",
                "Minimal spacing is 1", "Minimal separation is 0"})
        @DoubleRange(min = 0, max = 10)
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
            public static Optional<ResourceKey<Level>> singleBiomeDimension = Optional.empty();

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
        public static ResourceKey<Level> dimension = Level.OVERWORLD;

        @Config("Direction the player should look at initial spawn")
        public static WorldUtil.Directions direction = WorldUtil.Directions.SOUTH;

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
        }

        public static class Spawns {

            @Config("The range from island center for possible spawns to add. [default: 50]")
            public static int range = 50;

            @Config("Should players be able to modify their spawn positions? [default: false]")
            public static boolean modifySpawns = false;
        }
    }
}
