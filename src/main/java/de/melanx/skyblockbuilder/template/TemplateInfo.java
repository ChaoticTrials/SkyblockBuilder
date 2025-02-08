package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.providers.SpawnsProvider;
import de.melanx.skyblockbuilder.config.values.providers.SpreadsProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public record TemplateInfo(String name, String desc, String file, SpawnsProvider spawns, Offset offset,
                           String surroundingBlocks, SpreadsProvider spreads, int surroundingMargin) {

    public TemplateInfo(String name, String file, SpawnsProvider spawns) {
        this(name, "", file, spawns, new Offset(TemplatesConfig.defaultOffset, 0, TemplatesConfig.defaultOffset), "default", new SpreadsProvider.Reference("default"), 0);
    }

    public TemplateInfo(String name, String file, SpawnsProvider spawns, Offset offset) {
        this(name, "", file, spawns, offset, "default", new SpreadsProvider.Reference("default"), 0);
    }

    public record SpreadInfo(String file, BlockPos minOffset, BlockPos maxOffset, Origin origin) {

        public static final SpreadInfo DEFAULT = new SpreadInfo("default.nbt", BlockPos.ZERO, BlockPos.ZERO, Origin.ZERO);

        public enum Origin {
            CENTER,
            ZERO;

            public static BlockPos originOffset(Origin origin, StructureTemplate template) {
                return origin == CENTER ? new BlockPos(template.size.getX() / 2, template.size.getY() / 2, template.size.getZ() / 2) : BlockPos.ZERO;
            }
        }
    }

    public record Offset(int x, int y, int z) {}
}
