package jet.bpm.engine;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.commands.ExecutionCommand;

public class DefaultExecution implements Execution, Serializable {

    private final String id;
    private final String parentId;
    private final String processBusinessKey;
    private final Deque<ExecutionCommand> commands = new ArrayDeque<>();
    private final ExecutionContext context;
    private final Map<ActivationKey, Integer> activations = new HashMap<>();
    
    private boolean suspended = false;

    public DefaultExecution(String id, String parentId, String processBusinessKey, ExecutionContext context) {
        this.id = id;
        this.parentId = parentId;
        this.processBusinessKey = processBusinessKey;
        this.context = context;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public String getBusinessKey() {
        return processBusinessKey;
    }

    public ExecutionContext getContext() {
        return context;
    }
    
    @Override
    public boolean isDone() {
        return commands.isEmpty();
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public ExecutionCommand pop() {
        return commands.poll();
    }

    public void push(ExecutionCommand f) {
        commands.push(f);
    }

    public ExecutionCommand peek() {
        return commands.peek();
    }

    public int size() {
        return commands.size();
    }
    
    public void onActivation(Execution e, String processDefinitionId, String elementId) {
        inc(processDefinitionId, elementId);
    }
    
    private Integer inc(String processDefinitionId, String elementId) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        Integer i = activations.get(k);
        if (i == null) {
            i = 0;
        }
        i = i + 1;
        activations.put(k, i);
        return i;
    }
    
    public boolean isActivated(String processDefinitionId, String elementId) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        return activations.containsKey(k);
    }

    public int getActivationCount(String processDefinitionId, String elementId) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        Integer i = activations.get(k);
        return i != null ? i : 0;
    }

    public void addActivations(DefaultExecution source) {
        for (ActivationKey k : source.activations.keySet()) {
            inc(k.processDefinitionId, k.elementId);
        }
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
