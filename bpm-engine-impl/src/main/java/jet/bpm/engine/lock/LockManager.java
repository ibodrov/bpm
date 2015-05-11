package jet.bpm.engine.lock;

public interface LockManager {

    void lock(String processBusinessKey);

    void unlock(String processBusinessKey);
}
