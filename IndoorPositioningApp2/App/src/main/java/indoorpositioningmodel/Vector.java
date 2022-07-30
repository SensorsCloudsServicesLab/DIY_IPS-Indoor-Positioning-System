package indoorpositioningmodel;

import java.util.Objects;

public class Vector {
    public double x;
    public double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    public Vector scale(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public double getLength() {
        return Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    public Vector normalise() {
        this.x = x/getLength();
        this.y = y/getLength();
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position pos = (Position) obj;
            return pos.x == this.x && pos.y == this.y;
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}
