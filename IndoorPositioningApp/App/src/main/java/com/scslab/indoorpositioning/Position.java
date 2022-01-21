package com.scslab.indoorpositioning;

import java.util.Objects;

public class Position {

    public double x;
    public double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceFrom(Position pos) {
        return Math.sqrt((pos.y - this.y) * (pos.y - this.y) + (pos.x - this.x) * (pos.x - this.x));
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
