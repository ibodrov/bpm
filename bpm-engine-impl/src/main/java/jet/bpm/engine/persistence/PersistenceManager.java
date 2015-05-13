package jet.bpm.engine.persistence;

import java.util.UUID;
import jet.bpm.engine.DefaultExecution;

public interface PersistenceManager {

    void save(DefaultExecution execution);
    
    DefaultExecution get(UUID id);

    DefaultExecution remove(UUID id);
}
