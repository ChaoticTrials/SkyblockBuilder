package de.melanx.skyblockbuilder.config.common;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.List;
import java.util.Optional;

@RegisterConfig("dimensions")
public class DimensionsConfig {

    public static class Overworld {

        @Config("Should overworld generate as in default world type? [default: false]")
        public static boolean Default = false;

        @Config({"A list of biomes for each island in a circle shape around the island.",
                "First entry will be first circle of that radius. Second entry will be the next ring and the radius will be added to the previous radius.",
                "Example:",
                "  [",
                "    {",
                "      \"id\": \"minecraft:plains\",",
                "      \"radius\": 64",
                "    },",
                "    {",
                "      \"id\": \"minecraft:end_highlands\",",
                "      \"radius\": 32",
                "    }",
                "  ]"
        })
        public static List<UnregisteredCenterBiome> centeredBiomes = List.of();
    }

    public static class Nether {

        @Config("Should nether generate as in default world type? [default: false]")
        public static boolean Default = false;

        @Config("File name in template directory of a valid template containing a nether portal")
        public static Optional<String> netherPortalStructure = Optional.empty();

        @Config({"A list of biomes for each island in a circle shape around the island.",
                "First entry will be first circle of that radius. Second entry will be the next ring and the radius will be added to the previous radius.",
                "Example:",
                "  [",
                "    {",
                "      \"id\": \"minecraft:plains\",",
                "      \"radius\": 64",
                "    },",
                "    {",
                "      \"id\": \"minecraft:end_highlands\",",
                "      \"radius\": 32",
                "    }",
                "  ]"
        })
        public static List<UnregisteredCenterBiome> centeredBiomes = List.of();
    }

    public static class End {

        @Config("Should end generate as in default world type? [default: false]")
        public static boolean Default = false;

        @Config("Should the main island be generated as normal? [default: true]")
        public static boolean mainIsland = true;
    }

    public record UnregisteredCenterBiome(ResourceLocation id, int radius) {}
}
