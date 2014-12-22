package jet.scdp.bpm.engine;

import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.model.ProcessDefinition;

public interface ProcessDefinitionProvider {

    ProcessDefinition getById(String id) throws ExecutionException;
}
