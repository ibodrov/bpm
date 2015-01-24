package jet.bpm.engine.lock;

public class NoopLockManager implements LockManager {

    @Override
    public void lock(String processBusinnessKey) {
    }

    @Override
    public void unlock(String processBusinessKey) {
    }
}
