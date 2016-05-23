package jet.bpm.engine.model.diagram;

import java.io.Serializable;

public class Waypoint implements Serializable {

    private final double x;
    private final double y;

    public Waypoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
