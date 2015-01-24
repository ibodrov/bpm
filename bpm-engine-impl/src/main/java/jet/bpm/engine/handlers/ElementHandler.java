package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.commands.ProcessElementCommand;

public interface ElementHandler {

    void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException;
}
