package jet.bpm.engine.commands;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;

public class ProcessElementCommand implements ExecutionCommand {

    private final String processDefinitionId;
    private final String elementId;
    private final String groupId;
    private final boolean exclusive;

    public ProcessElementCommand(String processDefinitionId, String elementId) {
        this(processDefinitionId, elementId, null, false);
    }

    public ProcessElementCommand(String processDefinitionId, String elementId, String groupId, boolean exclusive) {
        this.processDefinitionId = processDefinitionId;
        this.elementId = elementId;
        this.groupId = groupId;
        this.exclusive = exclusive;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getElementId() {
        return elementId;
    }

    public String getGroupId() {
        return groupId;
    }

    /**
     * Indicates exclusiveness of this flow. Used to mark exclusive branches of
     * parallel flows.
     */
    public boolean isExclusive() {
        return exclusive;
    }
    
    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        engine.getElementHandler().handle(execution, this);

        // perform notification of element activation
        ExecutionContext ctx = execution.getContext();
        ctx.onActivation(execution, processDefinitionId, elementId);
        engine.fireOnElementActivation(execution, processDefinitionId, elementId);

        return execution;
    }
}
