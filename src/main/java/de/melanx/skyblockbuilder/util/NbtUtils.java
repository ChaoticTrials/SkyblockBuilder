package de.melanx.skyblockbuilder.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

public class NbtUtils {

    public static Optional<BlockPos> readBlockPos(CompoundTag tag, String key) {
        Optional<int[]> aint = tag.getIntArray(key);

        return aint.filter(ints -> ints.length == 3).map(ints -> new BlockPos(ints[0], ints[1], ints[2]));
    }

    public static Tag writeBlockPos(BlockPos pos) {
        return new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }
}
