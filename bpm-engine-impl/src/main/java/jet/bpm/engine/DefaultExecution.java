package jet.bpm.engine;

import java.io.Serializable;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.commands.ExecutionCommand;

public class DefaultExecution implements Execution, Serializable {

    private final UUID id;
    private final String processBusinessKey;
    private final Deque<ExecutionCommand> commands = new ConcurrentLinkedDeque<>();
    private final Map<ActivationKey, Integer> activations = new ConcurrentHashMap<>();
    
    private boolean suspended = false;
    private ExecutionContext context;

    public DefaultExecution(UUID id, String processBusinessKey, ExecutionContext context) {
        this.id = id;
        this.processBusinessKey = processBusinessKey;
        this.context = context;
    }

    @Override
    public UUID getId() {
        return id;
    }
    
    @Override
    public String getBusinessKey() {
        return processBusinessKey;
    }

    public ExecutionContext getContext() {
        return context;
    }
    
    public void setContext(ExecutionContext ctx) {
        this.context = ctx;
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
        inc(processDefinitionId, elementId, 1);
    }
    
    public Integer inc(String processDefinitionId, String elementId, int count) {
        ActivationKey k = new ActivationKey(processDefinitionId, elementId);
        Integer i = activations.get(k);
        if (i == null) {
            i = 0;
        }
        i = i + count;
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
            inc(k.processDefinitionId, k.elementId, 1);
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
