package jet.bpm.engine.lock;

import com.google.common.util.concurrent.Striped;
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
    public void lock(String processBusinessKey) {
        log.debug("lock ['{}'] -> locking...", processBusinessKey);
        locks.get(processBusinessKey).lock();
        log.debug("lock ['{}'] -> locked", processBusinessKey);
    }

    @Override
    public void unlock(String processBusinessKey) {
        locks.get(processBusinessKey).unlock();
        log.debug("lock ['{}'] -> unlocked", processBusinessKey);
    }
}
