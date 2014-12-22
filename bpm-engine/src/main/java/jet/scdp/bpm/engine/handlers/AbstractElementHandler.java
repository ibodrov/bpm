package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.ProcessDefinitionProvider;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.model.ProcessDefinition;

public abstract class AbstractElementHandler implements ElementHandler {

    private final AbstractEngine engine;

    public AbstractElementHandler(AbstractEngine engine) {
        this.engine = engine;
    }

    protected AbstractEngine getEngine() {
        return engine;
    }

    protected ProcessDefinition getProcessDefinition(ProcessElementCommand c) throws ExecutionException {
        return getProcessDefinition(c.getProcessDefinitionId());
    }

    protected ProcessDefinition getProcessDefinition(String processDefinitionId) throws ExecutionException {
        ProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        return provider.getById(processDefinitionId);
    }
}
