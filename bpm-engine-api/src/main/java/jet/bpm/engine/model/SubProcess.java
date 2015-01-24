package jet.bpm.engine.model;

import java.util.Collection;

public class SubProcess extends ProcessDefinition {

    public SubProcess(String id, Collection<AbstractElement> children) {
        super(id, children);
    }
}
