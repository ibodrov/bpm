package jet.bpm.engine.lock;

import com.google.common.util.concurrent.Striped;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StripedLockManagerImpl implements LockManager {

    private static final Logger log = LoggerFactory.getLogger(StripedLockManagerImpl.class);

    private final Striped<Lock> locks;

    public StripedLockManagerImpl(int concurrencyLevel) {
        this.locks = Striped.lock(concurrencyLevel);
    }

    @Override
    public void lock(UUID k) {
        log.debug("lock ['{}'] -> locking...", k);
        locks.get(k).lock();
        log.debug("lock ['{}'] -> locked", k);
    }

    @Override
    public void unlock(UUID k) {
        locks.get(k).unlock();
        log.debug("lock ['{}'] -> unlocked", k);
    }
}
