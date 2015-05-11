package jet.bpm.engine.leveldb;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.EventStorage;
import jet.bpm.engine.event.ExpiredEvent;
import jet.bpm.engine.leveldb.index.BusinessKeyEventIndex;
import jet.bpm.engine.leveldb.index.ExpiredEventIndex;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelDbEventStorage implements EventStorage {

    private final ExpiredEventIndex expiredEventLevelDbIndex;

    private final BusinessKeyEventIndex businessKeyEventLevelDbIndex;

    private final LevelDb eventDb;

    private final Serializer serializer;

    public LevelDbEventStorage(Configuration cfg, DBFactory dbFactory, Serializer serializer) {
        eventDb = new LevelDb(dbFactory, cfg.getEventPath(), cfg.isSyncWrite());

        LevelDb expiredEventIndexDb = new LevelDb(dbFactory, cfg.getExpiredEventIndexPath(), cfg.isSyncWrite());
        this.expiredEventLevelDbIndex = new ExpiredEventIndex(expiredEventIndexDb, serializer);

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
    public Event get(EventKey key) {
        byte[] eventBytes = eventDb.get(marshallKey(key));
        PersistentEvent persistentEvent = unmarshallEvent(eventBytes);
        return persistentEvent.getEvent();
    }

    @Override
    public Event remove(EventKey key) {
        byte[] keyBytes = marshallKey(key);
        byte[] eventBytes = eventDb.get(keyBytes);
        if (eventBytes == null) {
            return null;
        }

        eventDb.delete(keyBytes);

        PersistentEvent persistentEvent = unmarshallEvent(eventBytes);

        expiredEventLevelDbIndex.onRemove(persistentEvent);

        businessKeyEventLevelDbIndex.onRemove(persistentEvent);

        return persistentEvent.getEvent();
    }

    @Override
    public Collection<Event> find(String processBusinessKey) {
        List<Event> result = new ArrayList<>();

        Set<String> eventNames = businessKeyEventLevelDbIndex.list(processBusinessKey);
        for (String eventName : eventNames) {
            Event e = get(new EventKey(processBusinessKey, eventName));
            if (e != null) {
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public void add(EventKey key, Event event) {
        PersistentEvent persistentEvent = new PersistentEvent(UUID.randomUUID(), event);

        expiredEventLevelDbIndex.onAdd(persistentEvent);
        businessKeyEventLevelDbIndex.onAdd(persistentEvent);
        eventDb.put(marshallKey(key), marshalEvent(persistentEvent));
    }

    @Override
    public List<ExpiredEvent> findNextExpiredEvent(int maxEvents) {
        return expiredEventLevelDbIndex.list(new Date(), maxEvents);
    }

    private byte[] marshallKey(EventKey key) {
        String s = key.getProcessBusinessKey() + ":" + key.getEventName();
        return s.getBytes(Charset.forName("UTF-8"));
    }

    private byte[] marshalEvent(PersistentEvent event) {
        return serializer.toBytes(event);
    }

    private PersistentEvent unmarshallEvent(byte[] event) {
        if (event == null) {
            return null;
        }

        return (PersistentEvent) serializer.fromBytes(event);
    }

    private static final Logger log = LoggerFactory.getLogger(LevelDbEventStorage.class);

    public void dump() throws Exception {
        try (DBIterator it = eventDb.iterator();) {
            for (it.seekToFirst(); it.hasNext();) {
                Map.Entry<byte[], byte[]> entry = it.next();

                log.info(">>>>: {} -> {}", entry.getKey(), unmarshallEvent(entry.getValue()));
            }
        }

        businessKeyEventLevelDbIndex.dump();
        expiredEventLevelDbIndex.dump();
    }
}