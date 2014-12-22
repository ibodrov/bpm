package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;

public interface ElementHandler {

    void handle(Execution s, ProcessElementCommand c) throws ExecutionException;
}
