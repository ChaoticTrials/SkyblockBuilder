package de.melanx.skyblockbuilder.util;

// Adapted from https://stackoverflow.com/questions/398299/looping-in-a-spiral
public class Spiral {

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

    public int[] toIntArray() {
        return new int[]{this.x, this.y, this.dx, this.dy};
    }

    public static Spiral fromArray(int[] ints) {
        return new Spiral(ints[0], ints[1], ints[2], ints[3]);
    }
}
