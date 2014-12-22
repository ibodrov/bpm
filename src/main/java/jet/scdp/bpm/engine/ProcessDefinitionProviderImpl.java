package jet.scdp.bpm.engine;

import java.util.HashMap;
import java.util.Map;
import jet.scdp.bpm.model.ProcessDefinition;

public class ProcessDefinitionProviderImpl implements ProcessDefinitionProvider {

    private final Map<String, ProcessDefinition> defs = new HashMap<>();

    public void add(ProcessDefinition pd) {
        defs.put(pd.getId(), pd);
    }

    @Override
    public ProcessDefinition getById(String id) throws ExecutionException {
        ProcessDefinition pd = defs.get(id);
        if (pd == null) {
            throw new ExecutionException("Unknown process definition: " + id);
        }
        return pd;
    }
}
