package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionException;

public interface IndexedProcessDefinitionProvider {

    public IndexedProcessDefinition getById(String id) throws ExecutionException;
}
