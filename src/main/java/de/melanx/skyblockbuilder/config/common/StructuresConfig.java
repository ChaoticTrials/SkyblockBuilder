package de.melanx.skyblockbuilder.config.common;

import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.util.data.ResourceList;

@RegisterConfig("structures")
public class StructuresConfig {

    @Config({"All the structures that should be generated.",
            "A list with all possible structures can be found in config/skyblockbuilder/data/structures.txt",
            "WARNING: This only works for vanilla dimensions (Overworld, Nether, End)"})
    public static ResourceList generationStructures = new ResourceList(true, b -> {
        b.simple(new ResourceLocation("minecraft", "fortress"));
    });

    @Config({"All the features that should be generated.",
            "A list with all possible structures can be found in config/skyblockbuilder/data/features.txt",
            "INFO: The two default values are required for the obsidian towers in end. If this is missing, they will be first generated when respawning the dragon.",
            "WARNING: Some features like trees need special surface!",
            "WARNING: This only works for vanilla dimensions (Overworld, Nether, End)"})
    public static ResourceList generationFeatures = new ResourceList(true, b -> {
        b.simple(new ResourceLocation("minecraft", "end_spike"));
        b.simple(new ResourceLocation("minecraft", "end_gateway_return"));
    });
}
