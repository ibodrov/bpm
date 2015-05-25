package jet.bpm.engine.mvstore;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.EventStorage;
import jet.bpm.engine.event.ExpiredEvent;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public class MvStoreEventStorage implements EventStorage {

    private String baseDir;
    private MVStore store;
    private MVMap<UUID, Event> eventsMap;
    private MVMap<byte[], ExpiredEvent> expiredMap;
    private MVMap<String, Set<UUID>> businessKeysMap;
    
    public void start() {
        File f = new File(baseDir);
        f.mkdirs();
        f = new File(f, "store");
        
        store = new MVStore.Builder()
                .fileName(f.getAbsolutePath())
                .open();
        
        eventsMap = store.openMap("events");
        expiredMap = store.openMap("expired");
        businessKeysMap = store.openMap("bk");
        
    }
    
    public void stop() {
        store.close();
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    private byte[] toBytes(long l) {
        return ByteBuffer.allocate(8).putLong(l).array();
    }
    
    private long toLong(byte[] ab) {
        return ByteBuffer.wrap(ab).getLong();
    }
    
    private byte[] expiredKey(UUID id, long expiredAt) {
        long mostSigBits = id.getMostSignificantBits();
        long leastSigBits = id.getLeastSignificantBits();
        return ByteBuffer.allocate(8 + 8 + 8)
                .putLong(expiredAt)
                .putLong(mostSigBits)
                .putLong(leastSigBits)
                .array();
    }
    
    @Override
    public Event get(UUID id) {
        return eventsMap.get(id);
    }

    @Override
    public Event remove(UUID id) {
        Event e = eventsMap.remove(id);
        if (e == null) {
            return null;
        }
        
        if (e.getExpiredAt() != null) {
            byte[] k = expiredKey(e.getId(), e.getExpiredAt().getTime());
            expiredMap.remove(k);
        }
        
        String bk = e.getProcessBusinessKey();
        Set<UUID> keys = businessKeysMap.get(bk);
        if (keys != null) {
            keys.remove(e.getId());
        }
        if (keys.isEmpty()) {
            businessKeysMap.remove(bk);
        } else {
            businessKeysMap.put(bk, keys);
        }
        
        return e;
    }

    @Override
    public Collection<Event> find(String processBusinessKey) {
        List<Event> result = new ArrayList<>();

        Set<UUID> ids = businessKeysMap.get(processBusinessKey);
        for (UUID id : ids) {
            Event e = get(id);
            if (e != null) {
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public Collection<Event> find(String processBusinessKey, String eventName) {
        Collection<Event> events = find(processBusinessKey);
        for (Iterator<Event> i = events.iterator(); i.hasNext();) {
            Event e = i.next();
            if (!e.getName().equals(eventName)) {
                i.remove();
            }
        }
        return events;
    }

    @Override
    public void add(Event event) {
        eventsMap.put(event.getId(), event);
        if (event.getExpiredAt() != null) {
            ExpiredEvent x = new ExpiredEvent(event.getId(), event.getExpiredAt());
            expiredMap.put(expiredKey(event.getId(), event.getExpiredAt().getTime()), x);
        }
        
        Set<UUID> bk = businessKeysMap.get(event.getProcessBusinessKey());
        if (bk == null) {
            bk = new HashSet<>();
        }
        bk.add(event.getId());
        businessKeysMap.put(event.getProcessBusinessKey(), bk);
    }

    @Override
    public List<ExpiredEvent> findNextExpiredEvent(int maxEvents) {
        List<ExpiredEvent> result = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        byte[] start = toBytes(0);
        Cursor<byte[], ExpiredEvent> c = expiredMap.cursor(start);
        while (c.hasNext()) {
            long k = toLong(c.getKey());
            if (k > now) {
                break;
            }
            
            if (result.size() >= maxEvents) {
                break;
            }
            
            ExpiredEvent e = c.getValue();
            result.add(e);
            c.remove();
        }
        
        return result;
    }
}
