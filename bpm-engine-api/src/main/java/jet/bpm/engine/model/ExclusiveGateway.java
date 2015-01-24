package jet.bpm.engine.model;

public class ExclusiveGateway extends AbstractElement {

    private final String defaultFlow;

    public ExclusiveGateway(String id) {
        this(id, null);
    }

    public ExclusiveGateway(String id, String defaultFlow) {
        super(id);
        this.defaultFlow = defaultFlow;
    }

    public String getDefaultFlow() {
        return defaultFlow;
    }
}
