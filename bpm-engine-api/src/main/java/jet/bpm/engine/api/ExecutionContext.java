package jet.bpm.engine.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface ExecutionContext extends ActivationListener, Serializable {

    Object getVariable(String key);
    
    Map<String, Object> getVariables();

    void setVariable(String key, Object value);
    
    boolean hasVariable(String key);
    
    void removeVariable(String key);

    Set<String> getVariableNames();
    
    boolean isActivated(String processDefinitionId, String elementId);
}
