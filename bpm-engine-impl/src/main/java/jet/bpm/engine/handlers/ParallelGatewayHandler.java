package jet.bpm.engine.handlers;

import java.util.List;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.GatewayHelper;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;

public class ParallelGatewayHandler extends AbstractElementHandler {
    
    public ParallelGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        
        GatewayHelper.dec(c.getContext());
        if (GatewayHelper.isZero(c.getContext())) {
            // store forks count...
            ProcessDefinition pd = getProcessDefinition(c);
            List<SequenceFlow> flows = ProcessDefinitionUtils.findOutgoingFlows(pd, c.getElementId());
            GatewayHelper.inc(c.getContext(), flows.size());
            
            // ...and proceed
            FlowUtils.followFlows(getEngine(), s, c);
        }
    }
}
