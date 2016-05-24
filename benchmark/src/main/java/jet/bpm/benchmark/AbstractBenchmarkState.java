package jet.bpm.benchmark;

import java.util.HashMap;
import java.util.Map;
import jet.bpm.engine.DefaultEngine;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.event.InMemEventStorage;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.task.ServiceTaskRegistry;
import jet.bpm.engine.task.ServiceTaskRegistryImpl;

public abstract class AbstractBenchmarkState {
    
    private final Engine engine;

    public AbstractBenchmarkState(ProcessDefinition def) {
        DummyProcessDefinitionProvider defs = new DummyProcessDefinitionProvider();
        defs.publish(def.getId(), def);
        this.engine = new DefaultEngine(defs, new ServiceTaskRegistryImpl(), new InMemEventStorage());
    }

    public Engine getEngine() {
        return engine;
    }

    public static class DummyProcessDefinitionProvider implements ProcessDefinitionProvider {

        private final Map<String, ProcessDefinition> defs = new HashMap<>();

        public void publish(String id, ProcessDefinition def) {
            defs.put(id, def);
        }

        @Override
        public ProcessDefinition getById(String id) throws ExecutionException {
            return defs.get(id);
        }
    }

    public static class DummyServiceTaskRegistry implements ServiceTaskRegistry {

        @Override
        public void register(String key, Object instance) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getByKey(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }    
}
