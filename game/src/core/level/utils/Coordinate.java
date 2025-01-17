package core.level.utils;

import core.utils.Point;

/** Coordinate in the dungeon, based on array index. */
public class Coordinate {

    public int x;
    public int y;

    /**
     * Create a new Coordinate
     *
     * @param x x-Coordinate
     * @param y y-Coordinate
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy a coordinate
     *
     * @param copyFrom Coordinate to copy
     */
    public Coordinate(Coordinate copyFrom) {
        x = copyFrom.x;
        y = copyFrom.y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode nit designed";
        return x + y; // any arbitrary constant will do
    }

    /**
     * Convert Coordinate to Point
     *
     * @return
     */
    public Point toPoint() {
        return new Point(x, y);
    }

    /**
     * Creates a new Coordinate which has the sum of the Coordinates
     *
     * @param other which Coordinate to add
     * @return Coordinate where the values for x and y are added
     */
    public Coordinate add(Coordinate other) {
        return new Coordinate(this.x + other.x, this.y + other.y);
    }
}
