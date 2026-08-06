package de.melanx.skyblockbuilder.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.Util;

import java.util.stream.IntStream;

// Adapted from https://stackoverflow.com/questions/398299/looping-in-a-spiral
public class Spiral {

    public static final Codec<Spiral> CODEC = Codec.INT_STREAM.comapFlatMap(
            input -> Util.fixedSize(input, 4).map(ints -> new Spiral(ints[0], ints[1], ints[2], ints[3])),
            spiral -> IntStream.of(spiral.x, spiral.y, spiral.dx, spiral.dy)
    );

    private int x;
    private int y;
    private int dx;
    private int dy;

    public Spiral() {
        this(0, 0, 0, -1);
    }

    public Spiral(int x, int y, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    public int[] next() {
        if (this.x == this.y || this.x < 0 && this.x == -this.y || this.x > 0 && this.x == 1 - this.y) {
            int t = this.dx;
            this.dx = -this.dy;
            this.dy = t;
        }
        this.x += this.dx;
        this.y += this.dy;
        return new int[]{this.x, this.y};
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

}
