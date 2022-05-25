package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.util.WorldUtil;

public record TemplateInfo(String name, String desc, String file, String spawns, WorldUtil.Directions direction) {

    public TemplateInfo(String name, String file, String spawns, WorldUtil.Directions direction) {
        this(name, "", file, spawns, direction);
    }

    public TemplateInfo(String name, String file, String spawns) {
        this(name, "", file, spawns);
    }

    public TemplateInfo(String name, String desc, String file, String spawns) {
        this(name, desc, file, spawns, WorldUtil.Directions.SOUTH);
    }
}
