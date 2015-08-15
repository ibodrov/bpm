package jet.bpm.engine.testkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.ProcessDefinition;

public class TestProcessDefinitionProvider implements ProcessDefinitionProvider {

    private final Map<String, ProcessDefinition> m = new ConcurrentHashMap<>();

    public void add(ProcessDefinition d) {
        m.put(d.getId(), d);
    }

    @Override
    public ProcessDefinition getById(String id) throws ExecutionException {
        return m.get(id);
    }
}
