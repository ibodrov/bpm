package jet.bpm.engine.leveldb.index;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.event.ExpiredEvent;
import jet.bpm.engine.leveldb.LevelDb;
import jet.bpm.engine.leveldb.PersistentEvent;
import jet.bpm.engine.leveldb.Serializer;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpiredEventIndex {

    private static final Logger log = LoggerFactory.getLogger(ExpiredEventIndex.class);

    private final String currentMarker = UUID.randomUUID().toString();

    private final Serializer serializer;

    private final LevelDb levelDb;

    public ExpiredEventIndex(LevelDb levelDb, Serializer serializer) {
        this.levelDb = levelDb;
        this.serializer = serializer;
    }

    public void init() {
        levelDb.init();
    }

    public void close() {
        levelDb.close();
    }

    public void onAdd(PersistentEvent event) {
        Date expiredAt = event.getEvent().getExpiredAt();
        if (expiredAt == null) {
            return;
        }

        String processBusinessKey = event.getEvent().getProcessBusinessKey();
        String eventName = event.getEvent().getName();

        byte[] key = marshallKey(new IndexKey(event.getId(), expiredAt.getTime()));
        byte[] value = marshallValue(new IndexValue(processBusinessKey, eventName, expiredAt));
        levelDb.put(key, value);
    }

    public void onRemove(PersistentEvent event) {
        Date expiredAt = event.getEvent().getExpiredAt();
        if (expiredAt == null) {
            return;
        }

        byte[] key = marshallKey(new IndexKey(event.getId(), expiredAt.getTime()));
        levelDb.delete(key);
    }

    public List<ExpiredEvent> list(Date now, int maxEventsCount) {
        List<ExpiredEvent> result = new ArrayList<>();

        long nowTime = now.getTime();

        Map<IndexKey, IndexValue> processedItems = new HashMap<>();
        try (DBIterator it = levelDb.iterator();) {
            for (it.seekToFirst(); it.hasNext();) {
                Map.Entry<byte[], byte[]> entry = it.next();

                IndexKey k = unmarshallKey(entry.getKey());
                if (k.getExpiredAt() > nowTime) {
                    break;
                }

                if (result.size() >= maxEventsCount) {
                    break;
                }

                IndexValue v = unmarshallValue(entry.getValue());

                boolean isInProcess = currentMarker.equals(v.getProcessedMarker());

                if(!isInProcess) {
                    result.add(new ExpiredEvent(v.getProcessBusinessKey(), v.getEventName(), v.getExpiredAt()));
                    processedItems.put(k, v);
                } else {
                    log.warn("list ['{}', {}] -> found items in process", now, maxEventsCount);
                }
            }

            for (Map.Entry<IndexKey, IndexValue> item : processedItems.entrySet()) {
                IndexKey k = item.getKey();
                IndexValue v = item.getValue();

                v.setProcessedMarker(currentMarker);

                levelDb.put(marshallKey(k), marshallValue(v));
            }

            log.info("list ['{}', {}] -> done ({})", now, maxEventsCount, result.size());

            return result;
        } catch (Exception e) {
            log.error("list ['{}', {}] -> error", now, maxEventsCount, e);
            throw new RuntimeException("call 'list' error");
        }
    }

    private byte[] marshallKey(IndexKey key) {
        UUID id = key.getEventId();
        long expiredAt = key.getExpiredAt();

        long mostSigBits = id.getMostSignificantBits();
        long leastSigBits = id.getLeastSignificantBits();
        return ByteBuffer.allocate(8 + 8 + 8)
                .putLong(expiredAt)
                .putLong(mostSigBits)
                .putLong(leastSigBits)
                .array();
    }

    private IndexKey unmarshallKey(byte[] key) {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8).put(key);
        buffer.flip();
        long expiredAt = buffer.getLong();
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();

        return new IndexKey(new UUID(mostSigBits, leastSigBits), expiredAt);
    }

    private byte[] marshallValue(IndexValue eventBusinessKey) {
        return serializer.toBytes(eventBusinessKey);
    }

    private IndexValue unmarshallValue(byte[] value) {
        return (IndexValue) serializer.fromBytes(value);
    }

    public void dump() throws Exception {
        try (DBIterator it = levelDb.iterator();) {
            for (it.seekToFirst(); it.hasNext();) {
                Map.Entry<byte[], byte[]> entry = it.next();

                log.info(">>>>: {} -> {}", unmarshallKey(entry.getKey()), unmarshallValue(entry.getValue()));
            }
        }
    }

    private static final class IndexKey {

        private final UUID eventId;

        private final long expiredAt;

        public IndexKey(UUID eventId, long expiredAt) {
            this.eventId = eventId;
            this.expiredAt = expiredAt;
        }

        public UUID getEventId() {
            return eventId;
        }

        public long getExpiredAt() {
            return expiredAt;
        }
    }

    public static final class IndexValue {

        private String processedMarker;

        private final String processBusinessKey;
        private final String eventName;
        private final Date expiredAt;

        public IndexValue(String processBusinessKey, String eventName, Date expiredAt) {
            this.processBusinessKey = processBusinessKey;
            this.eventName = eventName;
            this.expiredAt = expiredAt;
        }

        public void setProcessedMarker(String processedMarker) {
            this.processedMarker = processedMarker;
        }

        public String getProcessedMarker() {
            return processedMarker;
        }

        public String getEventName() {
            return eventName;
        }

        public Date getExpiredAt() {
            return expiredAt;
        }

        public String getProcessBusinessKey() {
            return processBusinessKey;
        }
    }
}
