package jet.bpm.engine.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManagerImpl implements EventManager {

    private static final Logger log = LoggerFactory.getLogger(EventManagerImpl.class);

    private final Map<EventKey, Event> events = new HashMap<>();

    @Override
    public Event find(String processBusinessKey, String eventId) {
        synchronized (events) {
            EventKey k = new EventKey(processBusinessKey, eventId);
            return events.get(k);
        }
    }

    @Override
    public Collection<Event> find(String processBusinessKey) {
        synchronized (events) {
            Collection<Event> c = new ArrayList<>();
            for (Map.Entry<EventKey, Event> e : events.entrySet()) {
                EventKey k = e.getKey();
                if (k.processBusinessKey.equals(processBusinessKey)) {
                    c.add(e.getValue());
                }
            }
            return c;
        }
    }

    @Override
    public void register(String processBusinessKey, Event event) {
        synchronized (events) {
            EventKey k = new EventKey(processBusinessKey, event.getId());
            events.put(k, event);
            log.debug("register ['{}', '{}'] -> done", processBusinessKey, event);
        }
    }

    @Override
    public void clearGroup(String processBusinessKey, String groupId) {
        synchronized (events) {
            for (Iterator<Map.Entry<EventKey, Event>> i = events.entrySet().iterator(); i.hasNext();) {
                Map.Entry<EventKey, Event> entry = i.next();
                String otherKey = entry.getKey().processBusinessKey;
                String otherGid = entry.getValue().getGroupId();

                if (processBusinessKey.equals(otherKey) && groupId.equals(otherGid)) {
                    i.remove();

                    String id = entry.getValue().getExecutionId();
                    log.debug("clearGroup ['{}', '{}'] -> execution {} was removed (current size: {})", processBusinessKey, groupId, id, events.size());
                }
            }
        }
    }

    private static final class EventKey implements Serializable {

        private final String processBusinessKey;
        private final String eventId;

        public EventKey(String processBusinessKey, String eventId) {
            this.processBusinessKey = processBusinessKey;
            this.eventId = eventId;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + Objects.hashCode(this.processBusinessKey);
            hash = 23 * hash + Objects.hashCode(this.eventId);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EventKey other = (EventKey) obj;
            if (!Objects.equals(this.processBusinessKey, other.processBusinessKey)) {
                return false;
            }
            return Objects.equals(this.eventId, other.eventId);
        }
    }
}