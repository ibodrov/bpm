package jet.bpm.engine.event;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPersistenceManagerImpl implements EventPersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(EventPersistenceManagerImpl.class);

    private final EventStorage eventStorage;

    public EventPersistenceManagerImpl(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    @Override
    public Event get(UUID id) {
        Event result = eventStorage.get(id);
        log.debug("get ['{}'] -> done ({})", id, result != null);
        return result;
    }
    
    @Override
    public Event remove(UUID id) {
        Event result = eventStorage.remove(id);
        log.debug("remove ['{}'] -> done ({})", id, result != null);
        return result;
    }

    @Override
    public Collection<Event> find(String processBusinessKey, String eventName) {
        Collection<Event> result = eventStorage.find(processBusinessKey, eventName);
        log.debug("find ['{}', '{}'] -> done ({})", processBusinessKey, eventName, result.size());
        return result;
    }
    
    @Override
    public void add(Event event) throws ExecutionException {
        eventStorage.add(event);
        log.debug("register ['{}'] -> done", event);
    }

    @Override
    public void clearGroup(String processBusinessKey, UUID groupId) {
        int removed = 0;

        Collection<Event> evs = eventStorage.find(processBusinessKey);
        for(Event e : evs) {
            UUID otherGid = e.getGroupId();

            if (groupId.equals(otherGid)) {
                eventStorage.remove(e.getId());
                removed++;
            }
        }

        log.debug("clearGroup ['{}', '{}'] -> total: {}, removed: {}", processBusinessKey, groupId, evs.size(), removed);
    }

    @Override
    public List<ExpiredEvent> findNextExpiredEvent(int maxEvents) throws ExecutionException {
        List<ExpiredEvent> result = eventStorage.findNextExpiredEvent(maxEvents);
        log.debug("findNextExpiredEvent [{}] -> done ({})", maxEvents, result.size());
        return result;
    }
}
