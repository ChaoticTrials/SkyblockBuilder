package de.melanx.skyblockbuilder.config.common;

import com.google.common.collect.Maps;
import net.minecraft.Util;
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
            "These are resource lists. See https://moddingx.org/libx/org/moddingx/libx/util/data/ResourceList.html#use_resource_lists_in_configs"})
    public static Map<String, ResourceList> biomes = Util.make(Maps.newHashMap(), map -> {
        map.put(Level.OVERWORLD.location().toString(), new ResourceList(false, b -> b.parse("minecraft:*ocean*")));
        map.put(Level.NETHER.location().toString(), ResourceList.DENY_LIST);
    });

    @Config("Should a surface be generated in the dimensions? [default: false]")
    public static boolean surface = false;

    @Config({"The block settings for generating the different dimensions surfaces.", "Same format as flat world generation settings (blocks only)"})
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
