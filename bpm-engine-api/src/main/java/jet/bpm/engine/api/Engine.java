package jet.bpm.engine.api;

import java.util.Map;
import java.util.UUID;

public interface Engine {

    /**
     * Starts a new process process instance with the given ID.
     * @param processBusinessKey external process instance ID, must be unique.
     * @param processDefinitionId the id of the process definition, cannot be null.
     * @param variables variables to be passed, can be null.
     * @throws ExecutionException
     */
    void start(String processBusinessKey, String processDefinitionId, Map<String, Object> variables) throws ExecutionException;

    /**
     * Resumes a process instance, waiting for specific event.
     * @param processBusinessKey external process instance ID.
     * @param eventName the name of the event, cannot be null.
     * @param variables variables to be passed, can be null. Values with the same
     * name will be replaced.
     * @throws ExecutionException
     */
    void resume(String processBusinessKey, String eventName, Map<String, Object> variables) throws ExecutionException;

    /**
     * Resumes a process instance, waiting for specific event.
     * @param eventId ID of event.
     * @param variables variables to be passed, can be null. Values with the same
     * name will be replaced.
     * @throws ExecutionException
     */
    void resume(UUID eventId, Map<String, Object> variables) throws ExecutionException;
}
