package jet.bpm.engine.task;

import java.util.HashMap;
import java.util.Map;

public class ServiceTaskRegistryImpl implements ServiceTaskRegistry {

    private final Map<String, Object> items = new HashMap<>();

    @Override
    public void register(String key, Object instance) {
        items.put(key, instance);
    }

    @Override
    public Object getByKey(String key) {
        return items.get(key);
    }
}
