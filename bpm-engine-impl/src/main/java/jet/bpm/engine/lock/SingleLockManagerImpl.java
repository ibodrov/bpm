package jet.bpm.engine.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleLockManagerImpl implements LockManager {

    private static final Logger log = LoggerFactory.getLogger(SingleLockManagerImpl.class);
    
    private final Lock lock = new ReentrantLock();
    
    @Override
    public void lock(String processBusinnessKey) {
        log.debug("lock ['{}'] -> locking...", processBusinnessKey);
        lock.lock();
        log.debug("lock ['{}'] -> locked", processBusinnessKey);
    }

    @Override
    public void unlock(String processBusinessKey) {
        lock.unlock();
        log.debug("lock ['{}'] -> unlocked", processBusinessKey);
    }
}
