package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.ProcessDefinition;

/**
 * Обработчик элемента 'call activity' - вызов подпроцесса BPM.
 */
public class CallActivityElementHandler extends AbstractCallHandler {

    public CallActivityElementHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    protected ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException {
        ProcessDefinition pd = getProcessDefinition(c);
        CallActivity act = (CallActivity) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        ProcessDefinitionProvider provider = getEngine().getProcessDefinitionProvider();
        return provider.getById(act.getCalledElement());
    }

    @Override
    protected String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException {
        return sub.getId();
    }
}
