package jet.bpm.engine.leveldb.index;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.leveldb.LevelDb;
import jet.bpm.engine.leveldb.Serializer;

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

    public void onAdd(Event event) {
        String processBusinessKey = event.getProcessBusinessKey();

        byte[] key = marshallKey(processBusinessKey);
        Set<UUID> ids = list(processBusinessKey);
        if(ids.isEmpty()) {
            ids = new HashSet<>();
        }
        ids.add(event.getId());

        levelDb.put(key, marshallValue(ids));
    }

    public void onRemove(Event e) {
        String processBusinessKey = e.getProcessBusinessKey();
        Set<UUID> ids = list(processBusinessKey);
        if(ids == null) {
            return;
        }

        boolean removed = ids.remove(e.getId());
        if(!removed) {
            return;
        }

        byte[] key = marshallKey(e.getProcessBusinessKey());

        if(ids.isEmpty()) {
            levelDb.delete(key);
        } else {
            levelDb.put(key, marshallValue(ids));
        }
    }

    public Set<UUID> list(String processBusinessKey) {
        byte[] key = marshallKey(processBusinessKey);
        byte[] idsBytes = levelDb.get(key);
        if(idsBytes == null) {
            return Collections.<UUID>emptySet();
        }
        return unmarshallValue(idsBytes);
    }

    private byte[] marshallKey(String processBusinessKey) {
        return processBusinessKey.getBytes(Charset.forName("UTF-8"));
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> unmarshallValue(byte[] value) {
        return (Set<UUID>) serializer.fromBytes(value);
    }

    private byte[] marshallValue(Set<UUID> value) {
        return serializer.toBytes(value);
    }
}
