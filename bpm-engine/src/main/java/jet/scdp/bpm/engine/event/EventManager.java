package jet.scdp.bpm.engine.event;

import java.util.Collection;

public interface EventManager {

    Event find(String processBusinessKey, String eventId);
    
    Collection<Event> find(String processBusinessKey);

    void clearGroup(String processBusinessKey, String groupId);

    void register(String processBusinessKey, Event event);
}
