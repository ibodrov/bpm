package jet.bpm.engine.persistence;

import java.util.HashMap;
import java.util.Map;
import jet.bpm.engine.DefaultExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceManagerImpl implements PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManagerImpl.class);

    private final Map<String, DefaultExecution> states = new HashMap<>();

    @Override
    public DefaultExecution remove(String id) {
        synchronized (states) {
            DefaultExecution s = states.get(id);
            log.debug("remove ['{}'] -> done", id);
            return s;
        }
    }

    @Override
    public void save(DefaultExecution e) {
        synchronized (states) {
            states.put(e.getId(), e);
            log.debug("save [{}] -> done", e.getId());
        }
    }
}
