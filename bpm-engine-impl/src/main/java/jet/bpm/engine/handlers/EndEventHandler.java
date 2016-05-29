package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.BpmnErrorHelper;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ProcessDefinition;

public class EndEventHandler extends AbstractElementHandler {

    public EndEventHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        EndEvent e = (EndEvent) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        if (e.getErrorRef() != null) {
            // the element has an error ref - will raise an error to the parent
            // process.
            BpmnErrorHelper.raiseError(s.getContext(), e.getErrorRef());
        }
    }
}
