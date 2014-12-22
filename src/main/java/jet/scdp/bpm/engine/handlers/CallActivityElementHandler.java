package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.ProcessDefinitionProvider;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.model.CallActivity;
import jet.scdp.bpm.model.ProcessDefinition;

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
