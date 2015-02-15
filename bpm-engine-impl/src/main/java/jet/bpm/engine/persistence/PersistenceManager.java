package jet.bpm.engine.persistence;

import jet.bpm.engine.DefaultExecution;

public interface PersistenceManager {

    void save(DefaultExecution s);
    
    DefaultExecution remove(String id);
}
