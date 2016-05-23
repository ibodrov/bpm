package jet.bpm.engine.model.diagram;

import java.io.Serializable;

public class Bounds implements Serializable {

    private final double x;
    private final double y;
    private final double height;
    private final double width;

    public Bounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
