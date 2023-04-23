package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.WorldUtil;
import org.moddingx.libx.annotation.meta.RemoveIn;

public record TemplateInfo(String name, String desc, String file, String spawns, WorldUtil.Directions direction,
        @Deprecated(forRemoval = true) @RemoveIn(minecraft = "1.20") int offsetY,
        Offset offset, String surroundingBlocks, int surroundingMargin) {

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction) {
        this(name, "", file, spawns, direction, 0, new Offset(ConfigHandler.World.offset, ConfigHandler.World.offset), "default", 0);
    }

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction, Offset offset) {
        this(name, "", file, spawns, direction, 0, offset, "default", 0);
    }

    // todo 1.20 add y
    public record Offset(int x, int z) {
    }
}
