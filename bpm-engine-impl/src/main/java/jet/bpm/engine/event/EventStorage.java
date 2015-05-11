package jet.bpm.engine.event;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface EventStorage {

    Event get(EventKey key);

    Event remove(EventKey key);

    Collection<Event> find(String processBusinessKey);

    void add(EventKey k, Event event);

    List<ExpiredEvent> findNextExpiredEvent(int maxEvents);

    public static final class EventKey implements Serializable {

        private final String processBusinessKey;
        private final String eventName;

        public EventKey(String processBusinessKey, String eventName) {
            this.processBusinessKey = processBusinessKey;
            this.eventName = eventName;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + Objects.hashCode(this.processBusinessKey);
            hash = 23 * hash + Objects.hashCode(this.eventName);
            return hash;
        }

        public String getEventName() {
            return eventName;
        }

        public String getProcessBusinessKey() {
            return processBusinessKey;
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
            return Objects.equals(this.eventName, other.eventName);
        }
    }
}
