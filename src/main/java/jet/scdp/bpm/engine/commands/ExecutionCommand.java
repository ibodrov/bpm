package jet.scdp.bpm.engine.commands;

import java.io.Serializable;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.ExecutionContext;
import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionException;

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
    Execution exec(AbstractEngine engine, Execution execution) throws ExecutionException;

    ExecutionContext getContext();
}
