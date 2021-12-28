package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/*
 * Credits go to Botania authors
 */
public final class IslandPos {

    private final int x;
    private final int z;
    private BlockPos center;

    public IslandPos(int x, int z) {
        this(x, ConfigHandler.Spawn.height, z);
    }

    public IslandPos(int x, int y, int z) {
        this.x = x;
        this.z = z;
        this.center = new BlockPos(this.x * ConfigHandler.World.islandDistance + ConfigHandler.World.offset, y, this.z * ConfigHandler.World.islandDistance + ConfigHandler.World.offset);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void changeHeight(int y) {
        this.center = this.center.atY(y);
    }

    public static IslandPos fromTag(CompoundTag tag) {
        return new IslandPos(tag.getInt("IslandX"), tag.getInt("Height"), tag.getInt("IslandZ"));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("IslandX", this.x);
        tag.putInt("IslandZ", this.z);
        tag.putInt("Height", this.center.getY());
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
