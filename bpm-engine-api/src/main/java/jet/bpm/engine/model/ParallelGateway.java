package jet.bpm.engine.model;

public class ParallelGateway extends AbstractElement {
    
    private final String exit;

    public ParallelGateway(String id) {
        super(id);
        this.exit = null;
    }

    public ParallelGateway(String id, String exit) {
        super(id);
        this.exit = exit;
    }

    public String getExit() {
        return exit;
    }
}
