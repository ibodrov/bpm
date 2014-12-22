package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.engine.BpmnErrorHelper;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.model.EndEvent;
import jet.scdp.bpm.model.ProcessDefinition;

/**
 * Обработчик элемента 'end event'.
 */
public class EndEventHandler extends AbstractElementHandler {

    public EndEventHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(Execution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        EndEvent e = (EndEvent) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        if (e.getErrorRef() != null) {
            // на элементе задан error ref - нужно выбросить ошибку в
            // процесс-родитель
            BpmnErrorHelper.raiseError(c.getContext(), e.getErrorRef());
        }
    }
}
