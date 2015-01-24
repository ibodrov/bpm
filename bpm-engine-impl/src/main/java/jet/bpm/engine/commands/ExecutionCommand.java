package jet.bpm.engine.commands;

import java.io.Serializable;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;

/**
 * Команда выполнения процесса. Реализации обычно предназначены для изменения
 * состояния процесса (стека, переменных и т.д.).
 */
public interface ExecutionCommand extends Serializable {

    /**
     * Выполняет команду, применяя её к указанному состоянию процесса.
     * @param engine
     * @param execution
     * @return измененное или новое состояние процесса.
     * @throws ExecutionException
     */
    DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException;

    ExecutionContext getContext();
}
