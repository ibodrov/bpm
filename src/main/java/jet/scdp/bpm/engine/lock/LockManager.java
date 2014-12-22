package jet.scdp.bpm.engine.lock;

public interface LockManager {

    void lock(String processBusinnessKey);
    
    void unlock(String processBusinessKey);
}
