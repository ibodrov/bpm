package jet.bpm.engine.model;

public class BoundaryEvent extends AbstractElement {

    private final String attachedToRef;
    private final String errorRef;

    public BoundaryEvent(String id, String attachedToRef, String errorRef) {
        super(id);
        this.attachedToRef = attachedToRef;
        this.errorRef = errorRef;
    }

    public String getAttachedToRef() {
        return attachedToRef;
    }

    public String getErrorRef() {
        return errorRef;
    }
}
