package com.chaosbuffalo.mkwidgets.client.gui.math;

/**
 * Simple integer 2D vector used for GUI positions.
 */
public class Vec2i {
    public int x;
    public int y;

    /**
     * @param x x coordinate
     * @param y y coordinate
     */
    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a new vector with another vector subtracted from this one.
     *
     * @param other vector to subtract
     * @return resulting vector
     */
    public Vec2i subtract(Vec2i other) {
        return this.subtract(other.x, other.y);
    }

    public Vec2i subtract(int xVal, int yVal) {
        return this.add(-xVal, -yVal);
    }

    public Vec2i add(Vec2i other) {
        return this.add(other.x, other.y);
    }

    /**
     * Returns a new vector with the given delta applied.
     *
     * @param xVal x delta
     * @param yVal y delta
     * @return resulting vector
     */
    public Vec2i add(int xVal, int yVal) {
        return new Vec2i(x + xVal, y + yVal);
    }
}
