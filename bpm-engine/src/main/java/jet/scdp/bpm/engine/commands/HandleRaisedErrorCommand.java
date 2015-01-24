package jet.scdp.bpm.engine.commands;

import java.util.Collections;
import java.util.List;
import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.BpmnErrorHelper;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.api.ExecutionContext;
import jet.scdp.bpm.engine.FlowUtils;
import jet.scdp.bpm.engine.ProcessDefinitionProvider;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.model.BoundaryEvent;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;
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
            // ошибки не случилось, продолжаем как ни в чем не бывало
            FlowUtils.followFlows(engine, execution, context, processDefinitionId, elementId, groupId, exclusive);
            return execution;
        }

        ProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        ProcessDefinition pd = provider.getById(processDefinitionId);

        BoundaryEvent ev = ProcessDefinitionUtils.findBoundaryEvent(pd, elementId, errorRef);
        if (ev == null) {
            // попробуем найти boundary event без указания типа ошибки
            ev = ProcessDefinitionUtils.findBoundaryEvent(pd, elementId, null);
        }
        
        if (ev != null) {
            log.debug("apply ['{}', '{}'] -> handle boundary error '{}'", execution.getProcessBusinessKey(), elementId, errorRef);
            
            // считаем ошибку обработанной
            BpmnErrorHelper.clear(context);
            
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
