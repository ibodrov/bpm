package jet.scdp.bpm.engine.commands;

import java.util.Set;
import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.BpmnErrorHelper;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.api.ExecutionContext;
import jet.scdp.bpm.engine.ExecutionContextHelper;
import jet.scdp.bpm.model.VariableMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Команда объединения контекстов корневого и дочернего процессов. Предназначена
 * для выполнения после завершения дочернего процесса. При выполнении данной
 * команды, out-переменные дочернего процесса станут переменными корневого
 * (вызвавшего дочерний) процесса.
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

        // TODO: возможно стоит отрефакторить в виде некоторой "условной"
        // команды
        String errorRef = BpmnErrorHelper.getRaisedError(child);
        if (errorRef != null) {
            // передаем возникшую ошибку от подпроцесса к процессу-родителю
            BpmnErrorHelper.raiseError(context, errorRef);
            log.debug("raising error '{}'", errorRef);
            return execution;
        }

        // OUT-параметры
        ExecutionContextHelper.copyVariables(engine.getExpressionManager(), child, context, outVariables);

        return execution;
    }

    @Override
    public ExecutionContext getContext() {
        return context;
    }
}
