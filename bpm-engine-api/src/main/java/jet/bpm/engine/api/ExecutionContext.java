package jet.bpm.engine.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * The execution context. Provides access to the process variables.
 */
public interface ExecutionContext extends Serializable {
    
    /**
     * Key of latest handled error, contains "errorRef" of a boundary error
     * event. Can be accessed with
     * {@link #getVariable(ExecutionContext.ERROR_CODE_KEY)}.
     */
    public static final String ERROR_CODE_KEY = "errorCode";

    Object getVariable(String key);
    
    Map<String, Object> getVariables();

    void setVariable(String key, Object value);
    
    boolean hasVariable(String key);
    
    void removeVariable(String key);

    Set<String> getVariableNames();
}
