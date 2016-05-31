package jet.bpm.engine.commands;

import java.io.Serializable;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;

public interface ExecutionCommand extends Serializable {

    /**
     * Executes a command with the specified process state.
     * @param engine
     * @param execution
     * @return modified or new process state.
     * @throws ExecutionException
     */
    DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException;
}
