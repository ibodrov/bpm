package jet.bpm.engine.task;

public interface ServiceTaskRegistry {

    void register(String key, Object instance);

    Object getByKey(String key);
}
