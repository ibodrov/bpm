package jet.bpm.engine.event;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventStorage {
    
    Event get(UUID id);

    Event remove(UUID id);

    Collection<Event> find(String processBusinessKey);
    
    Collection<Event> find(String processBusinessKey, String eventName);

    void add(Event event);

    List<ExpiredEvent> findNextExpiredEvent(int maxEvents);
}
