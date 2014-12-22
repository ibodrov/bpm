package jet.scdp.bpm.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import jet.scdp.bpm.api.Execution;
import jet.scdp.bpm.engine.commands.ExecutionCommand;

public class DefaultExecution implements Execution {

    private final String id;
    private final String parentId;
    private final String processBusinessKey;
    private final Deque<ExecutionCommand> commands = new ArrayDeque<>();

    private transient boolean suspended = false;

    public DefaultExecution(String id, String processBusinessKey) {
        this(id, null, processBusinessKey);
    }

    public DefaultExecution(String id, String parentId, String processBusinessKey) {
        this.id = id;
        this.parentId = parentId;
        this.processBusinessKey = processBusinessKey;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getProcessBusinessKey() {
        return processBusinessKey;
    }

    public boolean isDone() {
        return commands.isEmpty();
    }

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
