package jet.bpm.engine.persistence;

import java.util.Map;
import jet.bpm.engine.DefaultExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPersistenceManager implements PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(MapPersistenceManager.class);
    
    private final Map<String, DefaultExecution> delegate;

    public MapPersistenceManager(Map<String, DefaultExecution> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void save(DefaultExecution s) {
        delegate.put(s.getId(), s);
        log.info("save ['{}'] -> done", s.getId());
    }
    
    @Override
    public DefaultExecution remove(String id) {
        DefaultExecution e = delegate.remove(id);
        log.debug("remove ['{}'] -> done (found: {})", id, e != null);
        return e;
    }    
}
