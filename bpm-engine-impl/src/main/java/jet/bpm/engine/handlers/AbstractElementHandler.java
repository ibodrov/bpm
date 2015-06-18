package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.IndexedProcessDefinition;
import jet.bpm.engine.IndexedProcessDefinitionProvider;
import jet.bpm.engine.commands.ProcessElementCommand;

public abstract class AbstractElementHandler implements ElementHandler {

    private final AbstractEngine engine;

    public AbstractElementHandler(AbstractEngine engine) {
        this.engine = engine;
    }

    protected AbstractEngine getEngine() {
        return engine;
    }

    protected IndexedProcessDefinition getProcessDefinition(ProcessElementCommand c) throws ExecutionException {
        return getProcessDefinition(c.getProcessDefinitionId());
    }

    protected IndexedProcessDefinition getProcessDefinition(String processDefinitionId) throws ExecutionException {
        IndexedProcessDefinitionProvider provider = engine.getProcessDefinitionProvider();
        return provider.getById(processDefinitionId);
    }
}
