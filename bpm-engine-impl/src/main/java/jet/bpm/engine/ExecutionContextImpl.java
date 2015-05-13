package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExecutionContextImpl implements ExecutionContext {
    
    private final ExecutionContext parent;
    private final Map<String, Object> variables = Collections.synchronizedMap(new HashMap<String, Object>());

    public ExecutionContextImpl(ExecutionContext parent) {
        this.parent = parent;
    }

    @Override
    public Object getVariable(String key) {
        Object v = variables.get(key);
        if (v == null && parent != null) {
            return parent.getVariable(key);
        }
        return v;
    }

    @Override
    public Map<String, Object> getVariables() {
        Map<String, Object> m = new HashMap<>();
        if (parent != null) {
            m.putAll(parent.getVariables());
        }
        
        m.putAll(variables);
        return m;
    }
    
    @Override
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    @Override
    public void removeVariable(String key) {
        if (variables.containsKey(key)) {
            variables.remove(key);
        } else if (parent != null) {
            parent.removeVariable(key);
        }
    }

    @Override
    public boolean hasVariable(String key) {
        if(variables.containsKey(key)) {
            return true;
        } else if (parent != null) {
            return parent.hasVariable(key);
        }
        
        return false;
    }
    
    @Override
    public Set<String> getVariableNames() {
        return Collections.unmodifiableSet(variables.keySet());
    }
}
