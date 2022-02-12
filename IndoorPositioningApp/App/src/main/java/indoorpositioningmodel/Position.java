package indoorpositioningmodel;

public class Position extends Vector {

    public Position(double x, double y) {
        super(x, y);
    }

    public double distanceFrom(Position pos) {
        return Math.sqrt((pos.y - this.y) * (pos.y - this.y) + (pos.x - this.x) * (pos.x - this.x));
    }

}
