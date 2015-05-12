package jet.bpm.engine.handlers;

import java.util.ArrayList;
import java.util.List;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.ProcessEventMappingCommand;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelGatewayHandler extends AbstractElementHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ParallelGatewayHandler.class);
    
    public ParallelGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        
        String eId = c.getElementId();
        String defId = c.getProcessDefinitionId();
        
        // join
        ProcessDefinition pd = getProcessDefinition(defId);
        List<SequenceFlow> in = ProcessDefinitionUtils.findIncomingFlows(pd, eId);
        
        int activated = s.getActivationCount(defId, eId) + 1; // add current activation
        int total = in.size();
        
        if (activated > total) {
            throw new ExecutionException("Incorrect number of activation of element '%s' in process '%s': %d", eId, defId, activated);
        } else if (activated == total) {
            log.debug("handle ['{}', '{}'] -> forking", s.getId(), c.getProcessDefinitionId());
            // fork
            List<SequenceFlow> out = ProcessDefinitionUtils.findOutgoingFlows(pd, eId);
            List<SequenceFlow> filtered = filter(s, c, out);
            
            List<SequenceFlow> inactive = new ArrayList<>(out);
            inactive.removeAll(filtered);
            processInactive(s, c, inactive);
            
            s.push(new ProcessEventMappingCommand());
            FlowUtils.followFlows(s, c.getProcessDefinitionId(), c.getElementId(), c.getElementId(), false, filtered);
        } else {
            log.debug("handle ['{}', '{}'] -> keep joining on '{}' (activated: {}, total: {})", s.getId(), c.getProcessDefinitionId(), eId, activated, total);
        }
    }
    
    protected List<SequenceFlow> filter(DefaultExecution s, ProcessElementCommand c, List<SequenceFlow> flows) {
        return flows;
    }
    
    protected void processInactive(DefaultExecution s, ProcessElementCommand c, List<SequenceFlow> inactive) {
        // nothing to do
    }
}
