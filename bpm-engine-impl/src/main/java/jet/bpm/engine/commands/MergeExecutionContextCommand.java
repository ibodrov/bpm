package jet.bpm.engine.commands;

import java.util.Set;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.BpmnErrorHelper;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.ExecutionContextHelper;
import jet.bpm.engine.model.VariableMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to merge contexts of parent and child processes. Designed to run
 * after child process ends. On its execution, the out variables of the child process
 * will become variables in parent process.
 */
public class MergeExecutionContextCommand implements ExecutionCommand {

    private static final Logger log = LoggerFactory.getLogger(MergeExecutionContextCommand.class);

    private final ExecutionContext context;
    private final ExecutionContext child;
    private final Set<VariableMapping> outVariables;

    public MergeExecutionContextCommand(ExecutionContext context, ExecutionContext child, Set<VariableMapping> outVariables) {
        this.context = context;
        this.child = child;
        this.outVariables = outVariables;
    }

    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        execution.pop();
        
        // TODO: refactor as conditional command?
        String errorRef = BpmnErrorHelper.getRaisedError(child);
        if (errorRef != null) {
            // perform error raise
            BpmnErrorHelper.raiseError(context, errorRef);
            log.debug("raising error '{}'", errorRef);
            return execution;
        }
        
        ExecutionContextHelper.copyVariables(engine.getExpressionManager(), child, context, outVariables);

        return execution;
    }

    @Override
    public ExecutionContext getContext() {
        return context;
    }
}
