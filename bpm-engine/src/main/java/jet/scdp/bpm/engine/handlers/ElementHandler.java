package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;

public interface ElementHandler {

    void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException;
}
