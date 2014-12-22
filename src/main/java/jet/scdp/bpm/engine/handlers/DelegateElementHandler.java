package jet.scdp.bpm.engine.handlers;

import java.util.HashMap;
import java.util.Map;
import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.ProcessDefinitionProvider;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.model.AbstractElement;
import jet.scdp.bpm.model.CallActivity;
import jet.scdp.bpm.model.EndEvent;
import jet.scdp.bpm.model.EventBasedGateway;
import jet.scdp.bpm.model.ExclusiveGateway;
import jet.scdp.bpm.model.InclusiveGateway;
import jet.scdp.bpm.model.IntermediateCatchEvent;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;
import jet.scdp.bpm.model.ServiceTask;
import jet.scdp.bpm.model.StartEvent;
import jet.scdp.bpm.model.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Делегирующий обработчик элементов процесса. Распознает тип элемента и
 * передает выполнение в обработчик конкретного типа элемента.
 */
public class DelegateElementHandler implements ElementHandler {

    private static final Logger log = LoggerFactory.getLogger(DelegateElementHandler.class);

    private final AbstractEngine engine;
    private final Map<String, ElementHandler> delegates = new HashMap<>();

    public DelegateElementHandler(AbstractEngine engine) {
        this.engine = engine;

        register(CallActivity.class, new CallActivityElementHandler(engine));
        register(EndEvent.class, new EndEventHandler(engine));
        register(EventBasedGateway.class, new EventBasedGatewayHandler(engine));
        register(InclusiveGateway.class, new InclusiveGatewayHandler(engine));
        register(ExclusiveGateway.class, new ExclusiveGatewayHandler(engine));
        register(IntermediateCatchEvent.class, new IntermediateCatchEventHandler(engine));
        register(SequenceFlow.class, new SequenceFlowHandler(engine));
        register(ServiceTask.class, new ServiceTaskHandler(engine));
        register(StartEvent.class, new StartEventHandler(engine));
        register(SubProcess.class, new SubProcessElementHandler(engine));
    }

    private void register(Class<? extends AbstractElement> k, ElementHandler h) {
        delegates.put(k.getSimpleName(), h);
    }

    @Override
    public void handle(Execution s, ProcessElementCommand c) throws ExecutionException {
        ProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        ProcessDefinition pd = provider.getById(c.getProcessDefinitionId());
        AbstractElement e = ProcessDefinitionUtils.findElement(pd, c.getElementId());

        String key = e.getClass().getSimpleName();
        log.debug("handle ['{}', '{}'] -> got {} ('{}')", s.getId(), c.getProcessDefinitionId(), key, e.getId());

        ElementHandler h = delegates.get(key);
        if (h != null) {
            h.handle(s, c);
        } else {
            throw new ExecutionException("Unsupported element " + e.getClass().getSimpleName() + " '" + e.getId() + "' of process '" + pd.getId() + "'");
        }
    }
}
