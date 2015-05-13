package jet.bpm.engine.persistence;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jet.bpm.engine.DefaultExecution;

public class DummyPersistenceManager extends MapPersistenceManager {

    public DummyPersistenceManager() {
        super(new ConcurrentHashMap<UUID, DefaultExecution>());
    }
}
