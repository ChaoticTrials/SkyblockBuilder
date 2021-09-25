package de.melanx.skyblockbuilder.util;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class PositionHelper {

    /**
     * Finds the nearest position based from the {@code center} with the matching {@code condition}. If nothing matches
     * withing the {@code maxStep}s, the original position will be returned.
     */
    public static BlockPos findPos(BlockPos center, Predicate<BlockPos> condition, int maxStep) {
        for (int i = 0; i < maxStep; i++) {
            Optional<BlockPos> result = doFindPos(center, condition, i);
            if (result.isPresent()) {
                return result.get();
            }
        }

        return center;
    }

    public static Optional<BlockPos> doFindPos(BlockPos center, Predicate<BlockPos> condition, int step) {
        return IntStream.range(0, step).boxed()
                .flatMap(i -> findInRing(center.offset(0, i, 0), condition, step)
                        .or(() -> findInRing(center.offset(0, -i, 0), condition, step)).stream())
                .findFirst()
                .or(() -> findInHorSpiral(center.offset(0, step, 0), condition, step))
                .or(() -> findInHorSpiral(center.offset(0, -step, 0), condition, step));
    }

    public static Optional<BlockPos> findInRing(BlockPos center, Predicate<BlockPos> condition, int size) {
        BlockPos.MutableBlockPos mpos = center.mutable();
        mpos.move(-size, 0, -size);
        for (int dx = 0; dx < 2 * size; dx++) {
            if (condition.test(mpos)) {
                return Optional.of(mpos.immutable());
            }

            mpos.move(1, 0, 0);
        }

        for (int dz = 0; dz < 2 * size; dz++) {
            if (condition.test(mpos)) {
                return Optional.of(mpos.immutable());
            }

            mpos.move(0, 0, 1);
        }

        for (int dx = 0; dx < 2 * size; dx++) {
            if (condition.test(mpos)) {
                return Optional.of(mpos.immutable());
            }

            mpos.move(-1, 0, 0);
        }

        for (int dz = 0; dz < 2 * size; dz++) {
            if (condition.test(mpos)) {
                return Optional.of(mpos.immutable());
            }

            mpos.move(0, 0, -1);
        }

        return Optional.empty();
    }

    public static Optional<BlockPos> findInHorSpiral(BlockPos center, Predicate<BlockPos> condition, int size) {
        BlockPos.MutableBlockPos mpos = center.mutable();
        int dx = 0;
        int dz = -1;

        for (int i = 0; i < size; i++) {
            if (mpos.getX() == mpos.getZ() || mpos.getX() == 0 && mpos.getX() == -mpos.getZ() || mpos.getX() > 0 && mpos.getX() == 1 - mpos.getZ()) {
                int t = dx;
                dx = -dz;
                dz = t;
            }
            mpos.offset(dx, 0, dz);

            if (condition.test(mpos)) {
                return Optional.of(mpos.immutable());
            }
        }

        return Optional.empty();
    }
}
