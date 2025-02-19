package de.melanx.skyblockbuilder.spreads;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.moddingx.libx.impl.codec.EnumCodec;

public interface SpreadInfo {

    String file();

    BlockPos minOffset();

    BlockPos maxOffset();

    Origin origin();

    enum Origin {
        CENTER,
        ZERO;

        public static final Codec<Origin> CODEC = EnumCodec.get(Origin.class);

        public static BlockPos originOffset(Origin origin, StructureTemplate template) {
            return origin == CENTER ? new BlockPos(template.size.getX() / 2, template.size.getY() / 2, template.size.getZ() / 2) : BlockPos.ZERO;
        }
    }
}
