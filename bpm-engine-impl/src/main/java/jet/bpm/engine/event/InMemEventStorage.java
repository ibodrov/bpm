package jet.bpm.engine.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InMemEventStorage implements EventStorage {

    private final Map<EventKey, Event> events = new HashMap<>();

    private final List<ExpiredEvent> eventsToExecute = new ArrayList<>();

    @Override
    public Event get(EventKey k) {
        synchronized (events) {
            return events.get(k);
        }
    }

    @Override
    public Event remove(EventKey k) {
        synchronized (events) {
            Event e = events.remove(k);

            removeEventExpiredEvent(e);
            return e;
        }
    }

    @Override
    public Collection<Event> find(String processBusinessKey) {
        synchronized (events) {
            Collection<Event> c = new ArrayList<>();
            for (Map.Entry<EventKey, Event> e : events.entrySet()) {
                EventKey k = e.getKey();
                if (k.getProcessBusinessKey().equals(processBusinessKey)) {
                    c.add(e.getValue());
                }
            }
            return c;
        }
    }

    @Override
    public void add(EventKey k, Event event) {
        synchronized (events) {
            events.put(k, event);

            if (event.getExpiredAt() != null) {
                eventsToExecute.add(new ExpiredEvent(k.getProcessBusinessKey(), k.getEventName(), event.getExpiredAt()));
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

        String processBusinessKey = e.getProcessBusinessKey();
        String eventName = e.getName();
        for (Iterator<ExpiredEvent> it = eventsToExecute.iterator(); it.hasNext();) {
            ExpiredEvent ee = it.next();
            if (processBusinessKey.equals(ee.getProcessBusinessKey()) && eventName.equals(ee.getEventName())) {
                it.remove();
            }
        }
    }
}
