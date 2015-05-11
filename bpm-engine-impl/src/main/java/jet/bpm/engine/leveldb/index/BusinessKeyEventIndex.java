package jet.bpm.engine.leveldb.index;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jet.bpm.engine.leveldb.LevelDb;
import jet.bpm.engine.leveldb.PersistentEvent;
import jet.bpm.engine.leveldb.Serializer;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessKeyEventIndex {

    private final LevelDb levelDb;

    private final Serializer serializer;

    public BusinessKeyEventIndex(LevelDb levelDb, Serializer serializer) {
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
        String processBusinessKey = event.getEvent().getProcessBusinessKey();

        byte[] key = marshallKey(processBusinessKey);
        Set<String> eventNames = list(processBusinessKey);
        if(eventNames.isEmpty()) {
            eventNames = new HashSet<>();
        }
        eventNames.add(event.getEvent().getName());

        levelDb.put(key, marshallValue(eventNames));
    }

    public void onRemove(PersistentEvent event) {
        String processBusinessKey = event.getEvent().getProcessBusinessKey();
        Set<String> eventNames = list(processBusinessKey);
        if(eventNames == null) {
            return;
        }

        boolean removed = eventNames.remove(event.getEvent().getName());
        if(!removed) {
            return;
        }

        byte[] key = marshallKey(event.getEvent().getProcessBusinessKey());

        if(eventNames.isEmpty()) {
            levelDb.delete(key);
        } else {
            levelDb.put(key, marshallValue(eventNames));
        }
    }

    public Set<String> list(String processBusinessKey) {
        byte[] key = marshallKey(processBusinessKey);
        byte[] eventNameBytes = levelDb.get(key);
        if(eventNameBytes == null) {
            return Collections.<String>emptySet();
        }
        return unmarshallValue(eventNameBytes);
    }

    private byte[] marshallKey(String processBusinessKey) {
        return processBusinessKey.getBytes(Charset.forName("UTF-8"));
    }

    @SuppressWarnings("unchecked")
    private Set<String> unmarshallValue(byte[] value) {
        return (Set<String>) serializer.fromBytes(value);
    }

    private byte[] marshallValue(Set<String> value) {
        return serializer.toBytes(value);
    }

    private static final Logger log = LoggerFactory.getLogger(BusinessKeyEventIndex.class);
    public void dump() throws Exception {
        try (DBIterator it = levelDb.iterator();) {
            for (it.seekToFirst(); it.hasNext();) {
                Map.Entry<byte[], byte[]> entry = it.next();

                log.info(">>>>: {} -> {}", entry.getKey(), unmarshallValue(entry.getValue()));
            }
        }
    }
}
