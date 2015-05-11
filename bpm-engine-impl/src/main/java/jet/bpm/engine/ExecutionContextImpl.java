package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jet.bpm.engine.api.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionContextImpl implements ExecutionContext {
    
    private static transient final Logger log = LoggerFactory.getLogger(ExecutionContext.class);

    private final ExecutionContext parent;
    private final Map<ActivationKey, Integer> activations = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();

    public ExecutionContextImpl() {
        this(null);
    }

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

    @Override
    public void onActivation(Execution e, String processDefinitionId, String elementId) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        Integer i = activations.get(k);
        if (i == null) {
            i = 0;
        }
        i = i + 1;
        activations.put(k, i);
        
        log.debug("onActivation ['{}', '{}'] -> done (count: {}, current size: {})", processDefinitionId, elementId, i, activations.size());
    }

    @Override
    public boolean isActivated(String processDefinitionId, String elementId) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        if (activations.containsKey(k)) {
            return true;
        } else if (parent != null) {
            return parent.isActivated(processDefinitionId, elementId);
        } else {
            return false;
        }
    }

    @Override
    public int getActivationCount(String processDefinitionId, String elementId) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        Integer i = activations.get(k);
        if (i != null) {
            return i;
        }

        if (parent != null) {
            return parent.getActivationCount(processDefinitionId, elementId);
        }

        return 0;
    }

    private static final class ActivationKey implements Serializable {

        private final String processDefinitionId;
        private final String elementId;

        public ActivationKey(String processDefinitionId, String elementId) {
            this.processDefinitionId = processDefinitionId;
            this.elementId = elementId;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode(this.processDefinitionId);
            hash = 79 * hash + Objects.hashCode(this.elementId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ActivationKey other = (ActivationKey) obj;
            if (!Objects.equals(this.processDefinitionId, other.processDefinitionId)) {
                return false;
            }
            return Objects.equals(this.elementId, other.elementId);
        }
    }
}
