package jet.bpm.engine.model.graphics;

import java.io.Serializable;

public class Label implements Serializable {

    private final Bounds bounds;

    public Label(Bounds bounds) {
        this.bounds = bounds;
    }

    public Bounds getBounds() {
        return bounds;
    }
}
