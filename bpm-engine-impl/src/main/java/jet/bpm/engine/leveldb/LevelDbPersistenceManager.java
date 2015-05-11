package jet.bpm.engine.leveldb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.charset.Charset;
import java.util.logging.Level;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.persistence.PersistenceManager;
import org.iq80.leveldb.DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelDbPersistenceManager implements PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(LevelDbPersistenceManager.class);

    private final LevelDb db;

    private final Serializer serializer;

    public LevelDbPersistenceManager(Configuration cfg, DBFactory dbFactory, Serializer serializer) {
        this.db = new LevelDb(dbFactory, cfg.getExecutionPath(), cfg.isSyncWrite());
        this.serializer = serializer;
    }

    public void init() {
        db.init();
    }

    public void close() {
        db.close();
    }

    @Override
    public void save(DefaultExecution execution) {
        byte[] key = marshallKey(execution.getId());
        db.put(key, marshallValue(execution));
        log.debug("save ['{}'] -> done", execution.getId());
    }

    @Override
    public DefaultExecution remove(String id) {
        byte[] key = marshallKey(id);
        byte[] bytes = db.get(key);

        return unmarshallValue(bytes);
    }

    private byte[] marshallKey(String id) {
        return id.getBytes(Charset.forName("UTF-8"));
    }

    private byte[] marshallValue(DefaultExecution execution) {
//        return serializer.toBytes(execution);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(execution);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("marshallValue -> error", e);
            return null;
        }
    }

    private DefaultExecution unmarshallValue(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

//        return (DefaultExecution) serializer.fromBytes(bytes);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis) {

                    @Override

                    protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        final String name = desc.getName();
                        try {
                            return Class.forName(name);
                        } catch (final ClassNotFoundException ex) {
                            return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
                        }
                    }

                }) {
            return (DefaultExecution) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("marshallValue -> error", e);
            return null;
        }
    }
}
