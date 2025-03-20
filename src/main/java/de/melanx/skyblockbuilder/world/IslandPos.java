package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/*
 * Credits go to Botania authors
 */
public final class IslandPos {

    public static final String ISLAND_X = "island_x";
    public static final String ISLAND_Z = "island_z";
    public static final String CENTER_POS = "center_pos";

    private final int x;
    private final int z;
    private BlockPos center;

    public IslandPos(Level level, int x, int z, ConfiguredTemplate template) {
        this(x,
                Mth.clamp(
                        WorldUtil.calcSpawnHeight(level,
                                IslandPos.calcX(x, template.getOffset()) + (template.getTemplate().getSize().getX() / 2),
                                IslandPos.calcZ(z, template.getOffset()) + (template.getTemplate().getSize().getZ() / 2)
                        ) + template.getOffset().getY(), level.getMinBuildHeight(), level.getMaxBuildHeight()),
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

    public static IslandPos fromTag(CompoundTag tag) {
        //noinspection OptionalGetWithoutIsPresent
        return new IslandPos(
                tag.getInt(ISLAND_X),
                tag.getInt(ISLAND_Z),
                NbtUtils.readBlockPos(tag, CENTER_POS).get()
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(ISLAND_X, this.x);
        tag.putInt(ISLAND_Z, this.z);
        tag.put(CENTER_POS, NbtUtils.writeBlockPos(this.center));
        return tag;
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
