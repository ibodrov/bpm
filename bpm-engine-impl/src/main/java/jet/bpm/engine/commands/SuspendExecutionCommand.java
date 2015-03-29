package jet.bpm.engine.commands;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;

/**
 * Process suspension command. Invokes saving of process state and interrupts
 * its execution.
 */
public class SuspendExecutionCommand implements ExecutionCommand {

    private final ExecutionContext context;

    public SuspendExecutionCommand(ExecutionContext context) {
        this.context = context;
    }

    @Override
    public DefaultExecution exec(AbstractEngine e, DefaultExecution s) throws ExecutionException {
        s.pop();
        
        s.setSuspended(true);
        e.getPersistenceManager().save(s);
        
        return s;
    }

    @Override
    public ExecutionContext getContext() {
        return context;
    }
}
