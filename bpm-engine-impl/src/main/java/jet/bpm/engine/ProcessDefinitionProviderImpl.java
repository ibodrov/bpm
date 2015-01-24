package jet.bpm.engine;

import java.util.HashMap;
import java.util.Map;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.ProcessDefinition;

public class ProcessDefinitionProviderImpl implements ProcessDefinitionProvider {

    private final Map<String, ProcessDefinition> defs = new HashMap<>();

    public void add(ProcessDefinition pd) {
        defs.put(pd.getId(), pd);
    }

    @Override
    public ProcessDefinition getById(String id) throws ExecutionException {
        ProcessDefinition pd = defs.get(id);
        if (pd == null) {
            throw new ExecutionException("Unknown process definition '%s'", id);
        }
        return pd;
    }
}
