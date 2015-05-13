package jet.bpm.engine.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemEventStorage implements EventStorage {

    private final Map<UUID, Event> events = new HashMap<>();
    private final List<ExpiredEvent> eventsToExecute = new ArrayList<>();

    @Override
    public Event get(UUID k) {
        synchronized (events) {
            return events.get(k);
        }
    }

    @Override
    public Event remove(UUID k) {
        synchronized (events) {
            Event e = events.remove(k);

            removeEventExpiredEvent(e);
            return e;
        }
    }
    
    @Override
    public Collection<Event> find(String processBusinessKey, String eventName) {
        synchronized (events) {
            Collection<Event> c = new ArrayList<>();
            for (Event e : events.values()) {
                if (e.getProcessBusinessKey().equals(processBusinessKey) && e.getName().equals(eventName)) {
                    c.add(e);
                }
            }
            return c;
        }
    }

    @Override
    public Collection<Event> find(String processBusinessKey) {
        synchronized (events) {
            Collection<Event> c = new ArrayList<>();
            for (Event e : events.values()) {
                if (e.getProcessBusinessKey().equals(processBusinessKey)) {
                    c.add(e);
                }
            }
            return c;
        }
    }

    @Override
    public void add(Event event) {
        synchronized (events) {
            events.put(event.getId(), event);

            if (event.getExpiredAt() != null) {
                eventsToExecute.add(new ExpiredEvent(event.getId(), event.getExpiredAt()));
            }
        }
    }

    @Override
    public List<ExpiredEvent> findNextExpiredEvent(int maxEvents) {
        List<ExpiredEvent> result = new ArrayList<>(maxEvents);
        synchronized (events) {
            Date now = new Date();
            for (Iterator<ExpiredEvent> it = eventsToExecute.iterator(); it.hasNext();) {
                ExpiredEvent e = it.next();
                Date expiredAt = e.getExpiredAt();
                if (now.after(expiredAt) || now.equals(expiredAt)) {
                    result.add(e);
                    it.remove();
                }
                if (result.size() >= maxEvents) {
                    break;
                }
            }
        }
        return result;
    }

    private void removeEventExpiredEvent(Event e) {
        if(e == null) {
            return;
        }
        
        for (Iterator<ExpiredEvent> it = eventsToExecute.iterator(); it.hasNext();) {
            ExpiredEvent ee = it.next();
            if (ee.geId().equals(e.getId())) {
                it.remove();
            }
        }
    }
}
