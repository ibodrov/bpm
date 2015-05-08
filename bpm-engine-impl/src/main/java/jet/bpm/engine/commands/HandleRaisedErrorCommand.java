package jet.bpm.engine.commands;

import java.util.Collections;
import java.util.List;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.BpmnErrorHelper;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleRaisedErrorCommand implements ExecutionCommand {

    private static final Logger log = LoggerFactory.getLogger(HandleRaisedErrorCommand.class);
    
    private final ExecutionContext context;
    private final String processDefinitionId;
    private final String elementId;
    private final String groupId;
    private final boolean exclusive;

    public HandleRaisedErrorCommand(ProcessElementCommand c) {
        this(c.getContext(), c.getProcessDefinitionId(), c.getElementId(), c.getGroupId(), c.isExclusive());
    }

    public HandleRaisedErrorCommand(ExecutionContext context, String processDefinitionId, String elementId, String groupId, boolean exclusive) {
        this.context = context;
        this.processDefinitionId = processDefinitionId;
        this.elementId = elementId;
        this.groupId = groupId;
        this.exclusive = exclusive;
    }

    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        execution.pop();

        String errorRef = BpmnErrorHelper.getRaisedError(context);
        if (errorRef == null) {
            // no errors was raised, will continue execution
            FlowUtils.followFlows(engine, execution, context, processDefinitionId, elementId, groupId, exclusive);
            return execution;
        }

        ProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        ProcessDefinition pd = provider.getById(processDefinitionId);

        BoundaryEvent ev = ProcessDefinitionUtils.findBoundaryEvent(pd, elementId, errorRef);
        if (ev == null) {
            // try to find boundary event without specified error reference
            ev = ProcessDefinitionUtils.findBoundaryEvent(pd, elementId, null);
        }
        
        if (ev != null) {
            log.debug("apply ['{}', '{}'] -> handle boundary error '{}'", execution.getBusinessKey(), elementId, errorRef);
            
            // error is handled
            BpmnErrorHelper.clear(context);
            
            // save errorRef for later
            context.setVariable(ExecutionContext.ERROR_CODE_KEY, errorRef);
            
            followFlows(execution, pd, ev.getId(), context);
        }

        return execution;
    }

    @Override
    public ExecutionContext getContext() {
        return context;
    }

    protected void followFlows(DefaultExecution s, ProcessDefinition pd, String elementId, ExecutionContext context) throws ExecutionException {
        List<SequenceFlow> flows = ProcessDefinitionUtils.findOutgoingFlows(pd, elementId);
        Collections.reverse(flows);
        for (SequenceFlow next : flows) {
            s.push(new ProcessElementCommand(pd.getId(), next.getId(), context));
        }
    }
}
