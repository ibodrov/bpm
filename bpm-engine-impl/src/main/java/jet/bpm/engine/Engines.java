package jet.bpm.engine;

import jet.bpm.engine.api.Engine;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.event.InMemEventStorage;
import jet.bpm.engine.task.ServiceTaskRegistry;

public final class Engines {

    public static Engine inMemory(ProcessDefinitionProvider pdp, ServiceTaskRegistry str) {
        EventPersistenceManager epm = new EventPersistenceManagerImpl(new InMemEventStorage());
        return new DefaultEngine(pdp, str, epm);
    }
    
    private Engines() {
    }
}
