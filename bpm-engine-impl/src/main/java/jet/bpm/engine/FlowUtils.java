package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import java.util.Collections;
import java.util.List;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlowUtils {

    private static final Logger log = LoggerFactory.getLogger(FlowUtils.class);

    public static void followFlows(AbstractEngine engine, DefaultExecution execution, ProcessElementCommand c) throws ExecutionException {
        followFlows(engine, execution, c.getContext(), c.getProcessDefinitionId(), c.getElementId(), c.getGroupId(), c.isExclusive());
    }

    public static void followFlows(AbstractEngine engine, DefaultExecution execution, ProcessElementCommand c, String elementId) throws ExecutionException {
        followFlows(engine, execution, c.getContext(), c.getProcessDefinitionId(), elementId, c.getGroupId(), c.isExclusive());
    }

    public static void followFlows(AbstractEngine engine, DefaultExecution execution, ProcessElementCommand c, String groupId, boolean exclusive) throws ExecutionException {
        followFlows(engine, execution, c.getContext(), c.getProcessDefinitionId(), c.getElementId(), groupId, exclusive);
    }

    public static void followFlows(AbstractEngine engine, DefaultExecution execution, ExecutionContext context, String processDefinitionId, String elementId, String groupId, boolean exclusive) throws ExecutionException {
        ProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        ProcessDefinition pd = provider.getById(processDefinitionId);
        List<SequenceFlow> flows = ProcessDefinitionUtils.findOutgoingFlows(pd, elementId);
        
        // reverse the collection, to fill up the stack in correct order
        Collections.reverse(flows);
        for (SequenceFlow next : flows) {
            log.debug("followFlows ['{}'] -> continuing from '{}', '{}' to {}", execution.getId(), processDefinitionId, elementId, next.getId());
            execution.push(new ProcessElementCommand(processDefinitionId, next.getId(), groupId, exclusive, context));
        }
    }

    private FlowUtils() {
    }
}
