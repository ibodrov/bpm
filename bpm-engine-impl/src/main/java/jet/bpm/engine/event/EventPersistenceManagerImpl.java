package jet.bpm.engine.event;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.event.EventStorage.EventKey;
import jet.bpm.engine.leveldb.LevelDbEventStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPersistenceManagerImpl implements EventPersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(EventPersistenceManagerImpl.class);

    private final EventStorage eventStorage;

    public EventPersistenceManagerImpl(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    @Override
    public Event get(String processBusinessKey, String eventId) {
        EventKey k = new EventKey(processBusinessKey, eventId);

        Event result = eventStorage.get(k);

        log.debug("get ['{}', '{}'] -> done ({})", processBusinessKey, eventId, result != null);

        return result;
    }

    @Override
    public Event remove(String processBusinessKey, String eventName) {
        EventKey k = new EventKey(processBusinessKey, eventName);
        Event result = eventStorage.remove(k);

        log.debug("remove ['{}', '{}'] -> done ({})", processBusinessKey, eventName, result != null);

        return result;
    }

    @Override
    public Collection<Event> find(String processBusinessKey) {
        Collection<Event> result = eventStorage.find(processBusinessKey);

        log.debug("find ['{}'] -> done ({})", processBusinessKey, result.size());

        return result;
    }

    @Override
    public void register(String processBusinessKey, Event event) throws ExecutionException {
        EventKey k = new EventKey(processBusinessKey, event.getName());

        eventStorage.add(k, event);

        log.debug("register ['{}', '{}', '{}'] -> done", processBusinessKey, event, event.getExpiredAt());
    }

    @Override
    public void clearGroup(String processBusinessKey, String groupId) {
        int removed = 0;

        Collection<Event> events = eventStorage.find(processBusinessKey);
        for(Event event : events) {
            String otherKey = event.getProcessBusinessKey();
            String otherGid = event.getGroupId();

            if (processBusinessKey.equals(otherKey) && groupId.equals(otherGid)) {
                eventStorage.remove(new EventKey(event.getProcessBusinessKey(), event.getName()));
                removed++;
            }
        }

        log.debug("clearGroup ['{}', '{}'] -> total: {}, removed: {}", processBusinessKey, groupId, events.size(), removed);
    }

    @Override
    public List<ExpiredEvent> findNextExpiredEvent(int maxEvents) throws ExecutionException {
        List<ExpiredEvent> result = eventStorage.findNextExpiredEvent(maxEvents);

        log.debug("findNextExpiredEvent [{}] -> done ({})", maxEvents, result.size());

        try {
            ((LevelDbEventStorage)eventStorage).dump();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(EventPersistenceManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
