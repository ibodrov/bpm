package jet.bpm.engine.model;

import java.util.Set;

public class CallActivity extends AbstractElement {

    private final String calledElement;
    private final Set<VariableMapping> in;
    private final Set<VariableMapping> out;
    
    public CallActivity(String id, String calledElement) {
        this(id, calledElement, null, null);
    }

    public CallActivity(String id, String calledElement, Set<VariableMapping> in, Set<VariableMapping> out) {
        super(id);
        this.calledElement = calledElement;
        this.in = in;
        this.out = out;
    }

    public String getCalledElement() {
        return calledElement;
    }

    public Set<VariableMapping> getIn() {
        return in;
    }

    public Set<VariableMapping> getOut() {
        return out;
    }
}
