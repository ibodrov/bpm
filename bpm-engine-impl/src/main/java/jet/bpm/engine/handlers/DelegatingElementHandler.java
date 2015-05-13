package jet.bpm.engine.handlers;

import java.util.HashMap;
import java.util.Map;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.ExclusiveGateway;
import jet.bpm.engine.model.InclusiveGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ParallelGateway;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import jet.bpm.engine.model.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detected element type and delegates its handling to the specific processor.
 */
public class DelegatingElementHandler implements ElementHandler {

    private static final Logger log = LoggerFactory.getLogger(DelegatingElementHandler.class);

    private final AbstractEngine engine;
    private final Map<String, ElementHandler> delegates = new HashMap<>();

    public DelegatingElementHandler(AbstractEngine engine) {
        this.engine = engine;

        register(CallActivity.class, new CallActivityElementHandler(engine));
        register(EndEvent.class, new EndEventHandler(engine));
        register(EventBasedGateway.class, new EventBasedGatewayHandler(engine));
        register(InclusiveGateway.class, new InclusiveGatewayHandler(engine));
        register(ExclusiveGateway.class, new ExclusiveGatewayHandler(engine));
        register(ParallelGateway.class, new ParallelGatewayHandler(engine));
        register(IntermediateCatchEvent.class, new IntermediateCatchEventHandler(engine));
        register(SequenceFlow.class, new SequenceFlowHandler(engine));
        register(ServiceTask.class, new ServiceTaskHandler(engine));
        register(StartEvent.class, new StartEventHandler(engine));
        register(SubProcess.class, new SubProcessElementHandler(engine));
    }

    private void register(Class<? extends AbstractElement> k, ElementHandler h) {
        delegates.put(k.getName(), h);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        ProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        ProcessDefinition pd = provider.getById(c.getProcessDefinitionId());
        AbstractElement e = ProcessDefinitionUtils.findElement(pd, c.getElementId());

        String key = e.getClass().getName();
        log.debug("handle ['{}', '{}'] -> got {} ('{}')", s.getId(), c.getProcessDefinitionId(), key, e.getId());

        ElementHandler h = delegates.get(key);
        if (h != null) {
            h.handle(s, c);
        } else {
            throw new ExecutionException("Unsupported element %s '%s' of process '%s'", e.getClass().getSimpleName(), e.getId(), pd.getId());
        }
    }
}
