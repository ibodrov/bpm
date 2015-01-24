package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.ProcessDefinition;

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
