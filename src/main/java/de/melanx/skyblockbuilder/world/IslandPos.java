package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/*
 * Credits go to Botania authors
 */
public final class IslandPos {

    public static final IslandPos CENTERED = new IslandPos(0, 0, BlockPos.ZERO);
    public static final MapCodec<IslandPos> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("island_x").forGetter(islandPos -> islandPos.x),
            Codec.INT.fieldOf("island_z").forGetter(islandPos -> islandPos.z),
            BlockPos.CODEC.fieldOf("center_pos").forGetter(islandPos -> islandPos.center)
    ).apply(instance, IslandPos::new));

    private final int x;
    private final int z;
    private BlockPos center;

    public IslandPos(Level level, int x, int z, ConfiguredTemplate template) {
        this(x,
                Mth.clamp(
                        WorldUtil.calcSpawnHeight(level,
                                IslandPos.calcX(x, template.getOffset()) + (template.getTemplate().getSize().getX() / 2),
                                IslandPos.calcZ(z, template.getOffset()) + (template.getTemplate().getSize().getZ() / 2)
                        ) + template.getOffset().getY(), level.getMinY(), level.getMaxY()),
                z, template.getOffset());
    }

    public IslandPos(int x, int y, int z, ConfiguredTemplate template) {
        this(x, y, z, template.getOffset());
    }

    public IslandPos(int x, int y, int z, BlockPos offset) {
        this.x = x;
        this.z = z;
        this.center = new BlockPos(IslandPos.calcX(x, offset), y, IslandPos.calcZ(z, offset));
    }

    private IslandPos(int x, int z, BlockPos center) {
        this.x = x;
        this.z = z;
        this.center = center;
    }

    private static int calcX(int x, BlockPos offset) {
        return x * WorldConfig.islandDistance + offset.getX() + TemplatesConfig.defaultOffset;
    }

    private static int calcZ(int z, BlockPos offset) {
        return z * WorldConfig.islandDistance + offset.getZ() + TemplatesConfig.defaultOffset;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void changeHeight(int y) {
        this.center = this.center.atY(y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof IslandPos)) {
            return false;
        }
        IslandPos islandPos = (IslandPos) o;
        return this.x == islandPos.x && this.z == islandPos.z;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.z;
        return result;
    }
}
