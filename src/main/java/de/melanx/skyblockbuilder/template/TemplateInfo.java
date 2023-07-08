package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.util.WorldUtil;

public record TemplateInfo(String name, String desc, String file, String spawns, WorldUtil.Directions direction,
        Offset offset, String surroundingBlocks, int surroundingMargin) {

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction) {
        this(name, "", file, spawns, direction, new Offset(WorldConfig.offset, 0, WorldConfig.offset), "default", 0);
    }

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction, Offset offset) {
        this(name, "", file, spawns, direction, offset, "default", 0);
    }

    public record Offset(int x, int y, int z) {
    }
}
