package jet.bpm.engine.event;

import java.util.Collection;
import java.util.List;
import jet.bpm.engine.api.ExecutionException;

public interface EventPersistenceManager {

    Event get(String processBusinessKey, String eventName);

    Event remove(String processBusinessKey, String eventName);

    Collection<Event> find(String processBusinessKey);

    void clearGroup(String processBusinessKey, String groupId);

    void register(String processBusinessKey, Event event) throws ExecutionException;

    List<ExpiredEvent> findNextExpiredEvent(int maxEvents) throws ExecutionException;
}
