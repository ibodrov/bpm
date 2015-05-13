package jet.bpm.engine.model.graphics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessGraphics implements Serializable {

    private final List<Shape> shapes = new ArrayList<>();
    
    private final List<Edge> edges = new ArrayList<>();

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Shape> getShapes() {
        return shapes;
    }
}
