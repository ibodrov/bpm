package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.ProcessDefinition;

public interface ProcessDefinitionProvider {

    ProcessDefinition getById(String id) throws ExecutionException;
}
