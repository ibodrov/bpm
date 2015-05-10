package jet.bpm.engine.event;

import java.util.Collection;
import java.util.List;
import jet.bpm.engine.api.ExecutionException;

public interface EventManager {

    Event find(String processBusinessKey, String eventId);

    Event remove(String processBusinessKey, String eventId);

    Collection<Event> find(String processBusinessKey);

    void clearGroup(String processBusinessKey, String groupId);

    void register(String processBusinessKey, Event event);

    List<Event> findNextEventsToExecute(int maxEvents) throws ExecutionException;
}
