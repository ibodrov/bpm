package jet.bpm.engine;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.commands.ExecutionCommand;

public class DefaultExecution implements Execution, Serializable {

    private final String id;
    private final String parentId;
    private final String processBusinessKey;
    private final Deque<ExecutionCommand> commands = new ArrayDeque<>();
    private final ExecutionContext context;
    
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
}
