package jet.bpm.engine.api;

/**
 * BPMN error, wrapped in an exception.
 */
public class BpmnError extends RuntimeException {

    private final String errorRef;

    public BpmnError(String errorRef) {
        this.errorRef = errorRef;
    }

    public String getErrorRef() {
        return errorRef;
    }
}
