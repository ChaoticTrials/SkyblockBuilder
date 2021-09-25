package de.melanx.skyblockbuilder.util;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PositionHelper {

    public static BlockPos findPos(BlockPos center, Predicate<BlockPos> condition, int maxStep) {
        for (int i = 0; i < maxStep; i++) {
            Optional<BlockPos> result = doFindPos(center, condition, i);
            if (result.isPresent()) return result.get();
        }

        return center;
    }

    // ugly, sorry
    public static Optional<BlockPos> doFindPos(BlockPos center, Predicate<BlockPos> condition, int step) {
        return PositionHelper.or(
                PositionHelper.or(IntStream.range(0, step).boxed()
                                .flatMap(i -> PositionHelper.stream(
                                        PositionHelper.or(findInRing(center.add(0, i, 0), condition, step),
                                                () -> findInRing(center.add(0, -i, 0), condition, step))))
                                .findFirst(),
                        () -> findInHorSpiral(center.add(0, step, 0), condition, step)
                ),
                () -> findInHorSpiral(center.add(0, -step, 0), condition, step));
    }

    public static Optional<BlockPos> findInRing(BlockPos center, Predicate<BlockPos> condition, int size) {
        BlockPos.Mutable mpos = center.toMutable();
        mpos.move(-size, 0, -size);
        for (int xd = 0; xd < 2 * size; xd++) {
            if (condition.test(mpos)) return Optional.of(mpos.toImmutable());
            mpos.move(1, 0, 0);
        }
        for (int zd = 0; zd < 2 * size; zd++) {
            if (condition.test(mpos)) return Optional.of(mpos.toImmutable());
            mpos.move(0, 0, 1);
        }
        for (int xd = 0; xd < 2 * size; xd++) {
            if (condition.test(mpos)) return Optional.of(mpos.toImmutable());
            mpos.move(-1, 0, 0);
        }
        for (int zd = 0; zd < 2 * size; zd++) {
            if (condition.test(mpos)) return Optional.of(mpos.toImmutable());
            mpos.move(0, 0, -1);
        }

        return Optional.empty();
    }

    public static Optional<BlockPos> findInHorSpiral(BlockPos center, Predicate<BlockPos> condition, int size) {
        BlockPos mpos = center.toMutable();
        int dx = 0;
        int dz = -1;

        for (int i = 0; i < size; i++) {
            if (mpos.getX() == mpos.getZ() || mpos.getX() == 0 && mpos.getX() == -mpos.getZ() || mpos.getX() > 0 && mpos.getX() == 1 - mpos.getZ()) {
                int t = dx;
                dx = -dz;
                dz = t;
            }
            mpos.add(dx, 0, dz);

            if (condition.test(mpos)) {
                return Optional.of(mpos.toImmutable());
            }
        }

        return Optional.empty();
    }

    // [Java 9 copy]
    public static <T> Optional<T> or(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional, Supplier<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (optional.isPresent()) {
            return optional;
        } else {
            //noinspection unchecked
            Optional<T> r = (Optional<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    // [Java 9 copy]
    public static <T> Stream<T> stream(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }
}
