package jet.bpm.engine.handlers;

import java.util.Collection;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;
import jet.bpm.engine.model.ParallelGateway;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;

public class ParallelGatewayHandler extends AbstractElementHandler {

    public ParallelGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        ParallelGateway g = (ParallelGateway) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        // add execution suspension after all flows are done
        if (g.getExit() != null) {
            s.push(new SuspendExecutionCommand(c.getContext()));
        }

        // will continue execution on one of two conditions:
        // - if current element is opening gateway and we just need to execute
        //   all of its outgoing flows;
        // - if current element is closing gateway (or gateway without join) and
        //   all of its flows are complete.
        if (g.getExit() != null || isActivationCompleted(pd, c)) {
            FlowUtils.followFlows(getEngine(), s, c);
        }
    }

    private boolean isActivationCompleted(ProcessDefinition pd, ProcessElementCommand c) throws ExecutionException {
        ExecutionContext ctx = c.getContext();

        Collection<SequenceFlow> flows = ProcessDefinitionUtils.findIncomingFlows(pd, c.getElementId());
        for (SequenceFlow flow : flows) {
            if (!ctx.isActivated(c.getProcessDefinitionId(), flow.getId())) {
                return false;
            }
        }

        return true;
    }
}
