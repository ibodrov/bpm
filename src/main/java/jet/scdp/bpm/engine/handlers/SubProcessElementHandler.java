package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.model.ProcessDefinition;

/**
 * Обработчик элемента 'sub-process'.
 */
public class SubProcessElementHandler extends AbstractCallHandler {

    public SubProcessElementHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    protected ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException {
        ProcessDefinition pd = getProcessDefinition(c);
        return ProcessDefinitionUtils.findSubProces(pd, c.getElementId());
    }

    @Override
    protected String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException {
        return c.getProcessDefinitionId();
    }
}
