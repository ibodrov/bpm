package jet.scdp.bpm.engine.commands;

import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.api.Engine;
import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionContext;
import jet.scdp.bpm.engine.ExecutionException;

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
    public Execution exec(AbstractEngine e, Execution s) throws ExecutionException {
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
