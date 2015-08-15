package jet.bpm.engine.leveldb;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.EventStorage;
import jet.bpm.engine.event.ExpiredEvent;
import jet.bpm.engine.leveldb.index.BusinessKeyEventIndex;
import jet.bpm.engine.leveldb.index.ExpiredEventIndex;
import org.iq80.leveldb.DBFactory;

public class LevelDbEventStorage implements EventStorage {

    private final ExpiredEventIndex expiredEventLevelDbIndex;
    private final BusinessKeyEventIndex businessKeyEventLevelDbIndex;
    private final LevelDb eventDb;
    private final Serializer serializer;

    public LevelDbEventStorage(Configuration cfg, DBFactory dbFactory, Serializer serializer) {
        eventDb = new LevelDb(dbFactory, cfg.getEventPath(), cfg.isSyncWrite());

        LevelDb expiredEventIndexDb = new LevelDb(dbFactory, cfg.getExpiredEventIndexPath(), cfg.isSyncWrite());
        this.expiredEventLevelDbIndex = new ExpiredEventIndex(expiredEventIndexDb);

        LevelDb businessKeyEventIndexDb = new LevelDb(dbFactory, cfg.getBusinessKeyEventIndexPath(), cfg.isSyncWrite());
        this.businessKeyEventLevelDbIndex = new BusinessKeyEventIndex(businessKeyEventIndexDb, serializer);

        this.serializer = serializer;
    }

    public void init() {
        try {
            eventDb.init();
            expiredEventLevelDbIndex.init();
            businessKeyEventLevelDbIndex.init();
        } catch (Exception e) {
            close();
        }
    }

    public void close() {
        eventDb.close();
        expiredEventLevelDbIndex.close();
        businessKeyEventLevelDbIndex.close();
    }

    @Override
    public Event get(UUID id) {
        byte[] eventBytes = eventDb.get(marshallKey(id));
        return unmarshallEvent(eventBytes);
    }

    @Override
    public Event remove(UUID id) {
        byte[] keyBytes = marshallKey(id);
        byte[] eventBytes = eventDb.get(keyBytes);
        if (eventBytes == null) {
            return null;
        }

        eventDb.delete(keyBytes);

        Event e = unmarshallEvent(eventBytes);
        expiredEventLevelDbIndex.onRemove(e);
        businessKeyEventLevelDbIndex.onRemove(e);
        return e;
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
    public Collection<Event> find(String processBusinessKey) {
        Collection<Event> result = new ArrayList<>();

        Set<UUID> ids = businessKeyEventLevelDbIndex.list(processBusinessKey);
        for (UUID id : ids) {
            Event e = get(id);
            if (e != null) {
                result.add(e);
            }
        }
        
        return result;
    }

    @Override
    public void add(Event event) {
        expiredEventLevelDbIndex.onAdd(event);
        businessKeyEventLevelDbIndex.onAdd(event);
        eventDb.put(marshallKey(event.getId()), marshalEvent(event));
    }

    @Override
    public List<ExpiredEvent> findNextExpiredEvent(int maxEvents) {
        return expiredEventLevelDbIndex.list(new Date(), maxEvents);
    }
    
    private static byte[] marshallKey(UUID id) {
        long mostSigBits = id.getMostSignificantBits();
        long leastSigBits = id.getLeastSignificantBits();
        return ByteBuffer.allocate(8 + 8)
                .putLong(mostSigBits)
                .putLong(leastSigBits)
                .array();
    }

    private byte[] marshalEvent(Event event) {
        return serializer.toBytes(event);
    }

    private Event unmarshallEvent(byte[] event) {
        if (event == null) {
            return null;
        }

        return (Event) serializer.fromBytes(event);
    }
}