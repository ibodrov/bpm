package jet.bpm.engine.persistence;

import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.DefaultExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapPersistenceManager implements PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(MapPersistenceManager.class);
    
    private final Map<UUID, DefaultExecution> delegate;

    public MapPersistenceManager(Map<UUID, DefaultExecution> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void save(DefaultExecution s) {
        delegate.put(s.getId(), s);
        log.info("save ['{}'] -> done", s.getId());
    }

    @Override
    public DefaultExecution get(UUID id) {
        return delegate.get(id);
    }
    
    @Override
    public DefaultExecution remove(UUID id) {
        DefaultExecution e = delegate.remove(id);
        log.debug("remove ['{}'] -> done (found: {})", id, e != null);
        return e;
    }    
}
