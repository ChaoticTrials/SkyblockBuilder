package de.melanx.skyblockbuilder.config.common;

import com.google.common.collect.Maps;
import de.melanx.skyblockbuilder.world.flat.FlatLayers;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.validate.IntRange;
import org.moddingx.libx.util.data.ResourceList;

import java.util.Map;

@RegisterConfig("world")
public class WorldConfig {

    @Config({"A list of biomes for each dimension.",
            "You can not use this for the end dimension. The end dimension will always have it's five biomes.",
            "Overworld has all oceans by default because animals cannot spawn in these biomes.",
            "A list of biomes will be generated at \"config/skyblockbuilder/data/biomes.txt\" each time joining a world.",
            "These are resource lists. See https://moddingx.org/libx/org/moddingx/libx/util/data/ResourceList.html#use_resource_lists_in_configs"})
    public static Map<String, ResourceList> biomes = Util.make(Maps.newHashMap(), map -> {
        map.put(Level.OVERWORLD.identifier().toString(), new ResourceList(false, b -> b.parse("minecraft:*ocean*")));
        map.put(Level.NETHER.identifier().toString(), ResourceList.DENY_LIST);
    });

    @Config("Should a surface be generated in the dimensions? [default: false]")
    public static boolean surface = false;

    @Config({"The block settings for generating the different dimensions surfaces.", "Same format as flat world generation settings (blocks only)"})
    public static Map<String, FlatLayers> surfaceSettings = Util.make(Maps.newHashMap(), map -> {
        map.put(Level.OVERWORLD.identifier().toString(), FlatLayers.of("minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block"));
        map.put(Level.NETHER.identifier().toString(), FlatLayers.EMPTY);
        map.put(Level.END.identifier().toString(), FlatLayers.EMPTY);
    });

    @Config({"A list of carvers for each dimension.",
            "A list of carvers will be generated at \"config/skyblockbuilder/data/carvers.txt\" each time joining a world.",
            "These are resource lists. See https://moddingx.org/libx/org/moddingx/libx/util/data/ResourceList.html#use_resource_lists_in_configs"})
    public static Map<String, ResourceList> carvers = Util.make(Maps.newHashMap(), map -> {
        map.put(Level.OVERWORLD.identifier().toString(), ResourceList.ALLOW_LIST);
        map.put(Level.NETHER.identifier().toString(), ResourceList.ALLOW_LIST);
        map.put(Level.END.identifier().toString(), ResourceList.ALLOW_LIST);
    });

    @Config("Sea level in world [default: 63]")
    public static int seaHeight = 63;

    @Config({"Distance between islands in overworld [default: 8192]"})
    @IntRange(min = 64, max = 29999900)
    public static int islandDistance = 8192;

    @Config("Prevent scheduled ticks after generating the island")
    public static boolean preventScheduledTicks = true;

    @Config("If a player is leaving a team, it will teleported to overworld spawn instead of spawn island.")
    public static boolean leaveToOverworld = false;

    public static class DimensionPerTeam {

        @Config({"EXPERIMENTAL - needs proper testing!",
                "Each team will get its own dimension for its island. Requires Infiniverse to be installed",
                "- https://www.curseforge.com/minecraft/mc-mods/infiniverse",
                "- https://modrinth.com/mod/infiniverse"})
        public static boolean enabled = false;

        @Config({"Should each team get its own overworld? If disabled, the vanilla overworld is shared with everyone.",
                "Always enabled if the spawn dimension is minecraft:overworld."})
        public static boolean overworld = true;

        @Config({"Should each team get its own nether? If disabled, the vanilla nether is shared with everyone.",
                "Always enabled if the spawn dimension is minecraft:the_nether."})
        public static boolean nether = true;
    }
}
