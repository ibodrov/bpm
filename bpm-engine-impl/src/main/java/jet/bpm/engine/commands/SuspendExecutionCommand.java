package jet.bpm.engine.commands;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;

/**
 * Команда приостановки процесса. Вызывает сохранение текущего состояния
 * процесса и его завершение. После приостановки процесс может быть возобновлен
 * с помощью {@link Engine#resume(java.lang.String, java.lang.String)}.
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
