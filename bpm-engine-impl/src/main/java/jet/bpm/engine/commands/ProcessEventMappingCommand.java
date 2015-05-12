package jet.bpm.engine.commands;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.EventMapHelper;
import jet.bpm.engine.api.ExecutionException;

public class ProcessEventMappingCommand implements ExecutionCommand {

    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        execution.pop();
        
        if (!EventMapHelper.isEmpty(execution)) {
            execution.push(new SuspendExecutionCommand());
        }
        
        return execution;
    }
}
