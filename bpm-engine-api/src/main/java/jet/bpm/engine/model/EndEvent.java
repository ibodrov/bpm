package jet.bpm.engine.model;

public class EndEvent extends AbstractElement {

    private final String errorRef;

    public EndEvent(String id) {
        this(id, null);
    }
    
    public EndEvent(String id, String errorRef) {
        super(id);
        this.errorRef = errorRef;
    }

    public String getErrorRef() {
        return errorRef;
    }
}
