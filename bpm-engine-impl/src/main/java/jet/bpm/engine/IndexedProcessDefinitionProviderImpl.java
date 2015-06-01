package jet.bpm.engine;

import java.util.HashMap;
import java.util.Map;
import jet.bpm.engine.api.ExecutionException;

public class IndexedProcessDefinitionProviderImpl implements IndexedProcessDefinitionProvider {

    private final Map<String, IndexedProcessDefinition> defs = new HashMap<>();
    
    public void add(IndexedProcessDefinition pd) {
        defs.put(pd.getId(), pd);
    }
    
    @Override
    public IndexedProcessDefinition getById(String id) throws ExecutionException {
        return defs.get(id);
    }
}
