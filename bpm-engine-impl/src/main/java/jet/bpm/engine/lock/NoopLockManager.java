package jet.bpm.engine.lock;

import java.util.UUID;

public class NoopLockManager implements LockManager {

    @Override
    public void lock(UUID k) {
    }

    @Override
    public void unlock(UUID k) {
    }
}
