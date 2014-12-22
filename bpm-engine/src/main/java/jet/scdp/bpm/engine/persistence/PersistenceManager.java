package jet.scdp.bpm.engine.persistence;

import jet.scdp.bpm.engine.DefaultExecution;

public interface PersistenceManager {

    void save(DefaultExecution s);

    DefaultExecution remove(String id);
}
