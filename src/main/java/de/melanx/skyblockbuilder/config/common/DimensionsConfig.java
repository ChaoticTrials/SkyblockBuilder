package de.melanx.skyblockbuilder.config.common;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.List;

@RegisterConfig("dimensions")
public class DimensionsConfig {

    public static class Overworld {

        @Config("Should overworld use custom generation? [default: true]")
        public static boolean isCustom = true;

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

        @Config("Should nether use custom generation? [default: true]")
        public static boolean isCustom = true;

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

        @Config("Should end use custom generation? [default: true]")
        public static boolean isCustom = true;

        @Config("Should the main island be generated as normal? [default: true]")
        public static boolean keepMainIsland = true;
    }

    public record UnregisteredCenterBiome(ResourceLocation id, int radius) {}
}
