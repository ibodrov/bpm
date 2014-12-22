package jet.scdp.bpm.engine.persistence;

import java.util.HashMap;
import java.util.Map;
import jet.scdp.bpm.engine.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceManagerImpl implements PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManagerImpl.class);

    private final Map<String, Execution> states = new HashMap<>();

    @Override
    public Execution remove(String id) {
        synchronized (states) {
            Execution s = states.get(id);
            log.debug("remove ['{}'] -> done", id);
            return s;
        }
    }

    @Override
    public void save(Execution e) {
        synchronized (states) {
            states.put(e.getId(), e);
            log.debug("save [{}] -> done", e.getId());
        }
    }
}
