package de.melanx.skyblockbuilder.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

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
        return new BlockPos(x << 8, 64, z << 8);
    }

    public static IslandPos fromTag(CompoundNBT tag) {
        return new IslandPos(tag.getInt("IslandX"), tag.getInt("IslandZ"));
    }

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("IslandX", x);
        tag.putInt("IslandZ", z);
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
        int result = x;
        result = 31 * result + z;
        return result;
    }
}
