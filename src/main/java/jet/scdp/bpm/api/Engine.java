package jet.scdp.bpm.api;

import java.util.Map;
import jet.scdp.bpm.engine.ExecutionException;

public interface Engine {

    void addListener(ActivationListener l);

    /**
     * Вызывает процесс и передает входные параметры.
     * @param processBusinessKey идентификатор конкретного процесса. Должен
     * быть уникальным (в рамках {@link Engine}).
     * @param processDefinitionId идентификатор вызываемого определения
     * процесса.
     * @param variables передаваемые параметры.
     * @throws ExecutionException 
     */
    void run(String processBusinessKey, String processDefinitionId, Map<String, Object> variables) throws ExecutionException;

    /**
     * Возобновляет процесс, ожидающий указанного события.
     * @param processBusinessKey идентификатор возобновляемого процесса.
     * @param eventId идентификатор события.
     * @param variables передаваемые параметры. Если в контексте процесса уже
     * были заданы переменные с передаваемыми ключами, то из значения будут
     * заменены.
     * @throws ExecutionException 
     */
    void resume(String processBusinessKey, String eventId, Map<String, Object> variables) throws ExecutionException;
}
