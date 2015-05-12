package jet.bpm.engine.commands;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.persistence.PersistenceManager;

public class PersistExecutionCommand implements ExecutionCommand {

    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        execution.pop();
        
        PersistenceManager pm = engine.getPersistenceManager();
        pm.save(execution);
        
        return execution;
    }
}
