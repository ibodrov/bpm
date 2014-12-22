package jet.scdp.bpm.engine.task;

import jet.scdp.bpm.engine.ExecutionContext;

public interface JavaDelegate {

    void execute(ExecutionContext ctx) throws Exception;
}
