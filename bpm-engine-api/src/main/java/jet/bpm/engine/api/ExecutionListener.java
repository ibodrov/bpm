package jet.bpm.engine.api;

import jet.bpm.engine.api.ExecutionContext;

public interface ExecutionListener {

    void notify(ExecutionContext ctx);
}
