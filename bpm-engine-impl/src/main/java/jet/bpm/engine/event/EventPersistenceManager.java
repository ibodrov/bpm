package jet.bpm.engine.event;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionException;

public interface EventPersistenceManager {

    Event get(UUID id);

    Event remove(UUID id);

    Collection<Event> find(String processBusinessKey, String eventName);

    void clearGroup(String processBusinessKey, UUID groupId);

    void add(Event event) throws ExecutionException;

    List<ExpiredEvent> findNextExpiredEvent(int maxEvents) throws ExecutionException;
}
