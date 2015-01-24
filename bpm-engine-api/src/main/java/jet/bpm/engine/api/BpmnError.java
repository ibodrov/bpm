package jet.bpm.engine.api;

/**
 * Класс исключения, с помощью которого можно передать BPMN error.
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
