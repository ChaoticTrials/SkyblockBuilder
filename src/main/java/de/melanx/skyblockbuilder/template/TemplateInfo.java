package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.common.WorldConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public record TemplateInfo(String name, String desc, String file, String spawns, Offset offset,
        String surroundingBlocks, String spreads, int surroundingMargin) {

    public TemplateInfo(String name, String file, String spawns) {
        this(name, "", file, spawns, new Offset(WorldConfig.offset, 0, WorldConfig.offset), "default", "default", 0);
    }

    public TemplateInfo(String name, String file, String spawns, Offset offset) {
        this(name, "", file, spawns, offset, "default", "default", 0);
    }

    public record SpreadInfo(String file, BlockPos minOffset, BlockPos maxOffset, Origin origin) {

        public static final SpreadInfo DEFAULT = new SpreadInfo("default.nbt", BlockPos.ZERO, BlockPos.ZERO, Origin.ZERO);

        public enum Origin {
            CENTER,
            ZERO;

            public static BlockPos originOffset(Origin origin, StructureTemplate template) {
                return switch (origin) {
                    case CENTER ->
                            new BlockPos(template.size.getX() / 2, template.size.getY() / 2, template.size.getZ() / 2);
                    default -> BlockPos.ZERO;
                };
            }
        }
    }

    public record Offset(int x, int y, int z) {}
}
