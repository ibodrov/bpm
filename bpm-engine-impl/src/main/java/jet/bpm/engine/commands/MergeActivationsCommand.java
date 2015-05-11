package jet.bpm.engine.commands;

import java.util.Collection;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeActivationsCommand implements ExecutionCommand {
    
    private static final Logger log = LoggerFactory.getLogger(MergeActivationsCommand.class);
    
    private final String groupId;

    public MergeActivationsCommand(String groupId) {
        this.groupId = groupId;
    }
    
    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        execution.pop();
        
        EventPersistenceManager epm = engine.getEventManager();
        PersistenceManager pm = engine.getPersistenceManager();
        
        Collection<Event> events = epm.find(execution.getBusinessKey());
        if (events == null) {
            return execution;
        }
        
        for (Event e : events) {
            if (groupId.equals(e.getGroupId())) {
                DefaultExecution x = pm.get(e.getExecutionId());
                x.addActivations(execution);
                pm.save(x);
                log.debug("exec ['{}'] -> added activations into '{}'", execution.getId(), x.getId());
            }
        }
        
        return execution;
    }
}
