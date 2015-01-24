package jet.scdp.bpm.api;

import jet.scdp.bpm.api.ExecutionContext;

public interface ExecutionListener {

    void notify(ExecutionContext ctx);
}
