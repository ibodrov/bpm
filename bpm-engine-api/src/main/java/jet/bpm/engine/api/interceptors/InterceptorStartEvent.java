package jet.bpm.engine.api.interceptors;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InterceptorStartEvent implements Serializable {
    
    private final String processBusinessKey;
    private final String processDefinitionId;
    private final UUID executionId;
    private final Map<String, Object> variables;

    public InterceptorStartEvent(String processBusinessKey, String processDefinitionId, UUID executionId, Map<String, Object> variables) {
        this.processBusinessKey = processBusinessKey;
        this.processDefinitionId = processDefinitionId;
        this.executionId = executionId;
        
        // TODO deep-copy the variables map
        this.variables = variables != null ? new HashMap<>(variables) : Collections.emptyMap();
    }

    public String getProcessBusinessKey() {
        return processBusinessKey;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public UUID getExecutionId() {
        return executionId;
    }
    
    public Collection<String> getVariableNames() {
        return variables.keySet();
    }
    
    /**
     * Returns a value of the process variable. <i>WARNING: returning value is
     * no a copy &mdash; modifying it is not recommended and could affect the
     * internal state of the process.</i>
     * @param name
     * @return 
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
}
