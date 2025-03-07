package de.melanx.skyblockbuilder.spreads;

import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.util.SkyCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface SpreadInfo {

    String file();

    BlockPos minOffset();

    BlockPos maxOffset();

    Origin origin();

    enum Origin {
        CENTER,
        ZERO;

        public static final Codec<Origin> CODEC = SkyCodecs.enumCodec(Origin.class);

        public static BlockPos originOffset(Origin origin, StructureTemplate template) {
            return origin == CENTER ? new BlockPos(template.size.getX() / 2, template.size.getY() / 2, template.size.getZ() / 2) : BlockPos.ZERO;
        }
    }
}
