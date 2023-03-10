package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.WorldUtil;

public record TemplateInfo(String name, String desc, String file, String spawns, WorldUtil.Directions direction, Offset offset) {

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction) {
        this(name, "", file, spawns, direction, new Offset(ConfigHandler.World.offset, ConfigHandler.World.offset));
    }

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction, Offset offset) {
        this(name, "", file, spawns, direction, offset);
    }

    public record Offset(int x, int z) {
    }
}
