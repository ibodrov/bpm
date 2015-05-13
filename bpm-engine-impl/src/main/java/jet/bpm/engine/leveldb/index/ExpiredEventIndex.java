package jet.bpm.engine.leveldb.index;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.ExpiredEvent;
import jet.bpm.engine.leveldb.LevelDb;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpiredEventIndex {

    private static final Logger log = LoggerFactory.getLogger(ExpiredEventIndex.class);
    private static final byte[] DUMMY = new byte[0];

    private final LevelDb db;

    public ExpiredEventIndex(LevelDb levelDb) {
        this.db = levelDb;    }

    public void init() {
        db.init();
    }

    public void close() {
        db.close();
    }

    public void onAdd(Event e) {
        Date expiredAt = e.getExpiredAt();
        if (expiredAt == null) {
            return;
        }

        byte[] key = marshallKey(e.getId(), expiredAt.getTime());
        byte[] value = DUMMY;
        db.put(key, value);
    }

    public void onRemove(Event e) {
        Date expiredAt = e.getExpiredAt();
        if (expiredAt == null) {
            return;
        }

        byte[] key = marshallKey(e.getId(), expiredAt.getTime());
        db.delete(key);
    }

    public List<ExpiredEvent> list(Date now, int maxEventsCount) {
        List<ExpiredEvent> result = new ArrayList<>();

        List<byte[]> toDelete = new ArrayList<>();
        try (DBIterator it = db.iterator();) {
            for (it.seekToFirst(); it.hasNext();) {
                Map.Entry<byte[], byte[]> entry = it.next();

                ExpiredEvent e = unmarshall(entry.getKey());
                if (e.getExpiredAt().after(now)) {
                    break;
                }

                if (result.size() >= maxEventsCount) {
                    break;
                }

                result.add(e);
                toDelete.add(entry.getKey());
            }

            db.delete(toDelete);

            log.info("list ['{}', {}] -> done ({})", now, maxEventsCount, result.size());
            return result;
        } catch (Exception e) {
            log.error("list ['{}', {}] -> error", now, maxEventsCount, e);
            throw new RuntimeException("call 'list' error", e);
        }
    }

    private byte[] marshallKey(UUID id, long expiredAt) {
        long mostSigBits = id.getMostSignificantBits();
        long leastSigBits = id.getLeastSignificantBits();
        return ByteBuffer.allocate(8 + 8 + 8)
                .putLong(expiredAt)
                .putLong(mostSigBits)
                .putLong(leastSigBits)
                .array();
    }

    private ExpiredEvent unmarshall(byte[] key) {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8).put(key);
        buffer.flip();
        long expiredAt = buffer.getLong();
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();

        return new ExpiredEvent(new UUID(mostSigBits, leastSigBits), new Date(expiredAt));
    }
}
