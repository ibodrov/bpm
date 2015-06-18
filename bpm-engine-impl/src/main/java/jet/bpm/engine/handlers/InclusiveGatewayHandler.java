package jet.bpm.engine.handlers;

import java.util.List;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.IndexedProcessDefinition;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InclusiveGatewayHandler extends ParallelGatewayHandler {
    
    private static final Logger log = LoggerFactory.getLogger(InclusiveGatewayHandler.class);

    public InclusiveGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    protected void processInactive(DefaultExecution s, ProcessElementCommand c, List<SequenceFlow> inactive) throws ExecutionException {
        if (inactive == null || inactive.isEmpty()) {
            return;
        }
        
        IndexedProcessDefinition pd = getProcessDefinition(c);
        String gwId = ProcessDefinitionUtils.findNextGatewayId(pd, c.getElementId());
        int count = inactive.size();
        log.debug("processInactive ['{}', '{}'] -> adding '{}' activations '{}' time(s)", s.getId(), c.getElementId(), gwId, count);
        s.inc(c.getProcessDefinitionId(), gwId, count);
    }
}
