package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.common.WorldConfig;
import net.minecraft.core.BlockPos;

public record TemplateInfo(String name, String desc, String file, String spawns, Offset offset,
        String surroundingBlocks, String spreads, int surroundingMargin) {

    public TemplateInfo(String name, String file, String spawns) {
        this(name, "", file, spawns, new Offset(WorldConfig.offset, 0, WorldConfig.offset), "default", "default", 0);
    }

    public TemplateInfo(String name, String file, String spawns, Offset offset) {
        this(name, "", file, spawns, offset, "default", "default", 0);
    }

    public record SpreadInfo(String file, BlockPos minOffset, BlockPos maxOffset) {

        public static final SpreadInfo DEFAULT = new SpreadInfo("default.nbt", BlockPos.ZERO, BlockPos.ZERO);
    }

    public record Offset(int x, int y, int z) {}
}
