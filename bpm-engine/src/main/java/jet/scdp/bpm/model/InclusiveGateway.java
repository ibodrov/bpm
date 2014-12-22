package jet.scdp.bpm.model;

public class InclusiveGateway extends AbstractElement {

    private final String exit;

    public InclusiveGateway(String id) {
        this(id, null);
    }

    public InclusiveGateway(String id, String exit) {
        super(id);
        this.exit = exit;
    }

    public String getExit() {
        return exit;
    }
}
