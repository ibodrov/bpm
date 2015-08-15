package jet.bpm.engine;

import java.util.HashMap;
import java.util.Map;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.ProcessDefinition;

public class TestProcessDefinitionProvider implements ProcessDefinitionProvider {

    private final Map<String, ProcessDefinition> defs = new HashMap<>();
    
    @Override
    public ProcessDefinition getById(String id) throws ExecutionException {
        return defs.get(id);
    }
    
    public void add(ProcessDefinition pd) {
        defs.put(pd.getId(), pd);
    }    
}
