package jet.bpm.engine.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Execution context. Provides access to the process variables.
 */
public interface ExecutionContext extends ActivationListener, Serializable {
    
    /**
     * Key of latest handled error, contains "errorRef" of boundary error event.
     * Can be accessed with {@link #getVariable(java.lang.String)}.
     */
    public static final String ERROR_CODE_KEY = "errorCode";

    Object getVariable(String key);
    
    Map<String, Object> getVariables();

    void setVariable(String key, Object value);
    
    boolean hasVariable(String key);
    
    void removeVariable(String key);

    Set<String> getVariableNames();

    /**
     * Indicates when specified element is activated (completed is execution).
     * @param processDefinitionId the ID of the process definition.
     * @param elementId the ID of the activated element.
     */
    boolean isActivated(String processDefinitionId, String elementId);
    
    int getActivationCount(String processDefinitionId, String elementId);
}
