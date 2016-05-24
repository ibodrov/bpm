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

public abstract class AbstractBenchmarkState {
    
    private final Engine engine;
    private final ServiceTaskRegistry serviceTaskRegistry;
    
    public AbstractBenchmarkState(ProcessDefinition def) {
        this.serviceTaskRegistry = new DummyServiceTaskRegistry();
        
        DummyProcessDefinitionProvider defs = new DummyProcessDefinitionProvider();
        defs.publish(def.getId(), def);
        this.engine = new DefaultEngine(defs, serviceTaskRegistry, new InMemEventStorage());
    }

    public final Engine getEngine() {
        return engine;
    }

    public final ServiceTaskRegistry getServiceTaskRegistry() {
        return serviceTaskRegistry;
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

        private final Map<String, Object> tasks = new HashMap<>();
        
        @Override
        public void register(String key, Object instance) {
            tasks.put(key, instance);
        }

        @Override
        public Object getByKey(String key) {
            return tasks.get(key);
        }
    }    
}
