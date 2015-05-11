package jet.bpm.engine.leveldb;

import java.io.Serializable;
import java.util.UUID;
import jet.bpm.engine.event.Event;

public class PersistentEvent implements Serializable {

    private final UUID id;

    private final Event event;

    public PersistentEvent(UUID id, Event event) {
        this.id = id;
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PersistentEvent{" + "id=" + id + ", event=" + event + '}';
    }
}
