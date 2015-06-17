package jet.bpm.engine.lock;

import java.util.UUID;

public interface LockManager {

    void lock(UUID k);

    void unlock(UUID k);
}
