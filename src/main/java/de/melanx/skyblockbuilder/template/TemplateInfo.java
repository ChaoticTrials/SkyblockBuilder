package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.common.WorldConfig;

public record TemplateInfo(String name, String desc, String file, String spawns, Offset offset,
        String surroundingBlocks, int surroundingMargin) {

    public TemplateInfo(String name, String file, String spawns) {
        this(name, "", file, spawns, new Offset(WorldConfig.offset, 0, WorldConfig.offset), "default", 0);
    }

    public TemplateInfo(String name, String file, String spawns, Offset offset) {
        this(name, "", file, spawns, offset, "default", 0);
    }

    public record Offset(int x, int y, int z) {
    }
}
