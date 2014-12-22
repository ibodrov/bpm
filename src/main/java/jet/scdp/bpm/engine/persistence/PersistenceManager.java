package jet.scdp.bpm.engine.persistence;

import jet.scdp.bpm.engine.Execution;

public interface PersistenceManager {

    void save(Execution s);

    Execution remove(String id);
}
