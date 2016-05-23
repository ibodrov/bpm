package jet.bpm.engine.model.diagram;

import java.io.Serializable;
import java.util.List;

public class Edge implements Serializable {

    private final String id;

    private final String elementId;

    private final Label label;
    
    private final List<Waypoint> waypoints;
    
    public Edge(String id, String elementId, Label label, List<Waypoint> waypoints) {
        this.id = id;
        this.elementId = elementId;
        this.label = label;
        this.waypoints = waypoints;
    }

    public String getElementId() {
        return elementId;
    }

    public String getId() {
        return id;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Label getLabel() {
        return label;
    }
}
