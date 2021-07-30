package de.melanx.skyblockbuilder.world;

import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/*
 * Credits go to Botania authors
 */
public final class IslandPos {

    private final int x;
    private final int z;

    public IslandPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public BlockPos getCenter() {
        return new BlockPos(this.x * LibXConfigHandler.World.islandDistance + LibXConfigHandler.World.offset, LibXConfigHandler.Spawn.height, this.z * LibXConfigHandler.World.islandDistance + LibXConfigHandler.World.offset);
    }

    public static IslandPos fromTag(CompoundTag tag) {
        return new IslandPos(tag.getInt("IslandX"), tag.getInt("IslandZ"));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("IslandX", this.x);
        tag.putInt("IslandZ", this.z);
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
