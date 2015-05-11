package jet.bpm.engine.leveldb;

import java.io.File;
import java.io.IOException;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelDb {

    private static final Logger log = LoggerFactory.getLogger(LevelDb.class);

    private final DBFactory dbFactory;

    private DB db;

    private final String path;

    private final ReadOptions readOptions;
    private final WriteOptions writeOptions;

    public LevelDb(DBFactory dbFactory, String path, boolean syncWrite) {
        this.dbFactory = dbFactory;
        this.path = path;
        this.readOptions = new ReadOptions();
        this.writeOptions = new WriteOptions().sync(syncWrite);
    }

    public void init() {
        try {
            db = openDatabase(dbFactory, path, dbOptions());
        } catch (Exception e) {
            log.error("init ['{}'] -> error", path, e);
            throw new RuntimeException("Unable to start database: '" + path + "'");
        }
    }

    public void close() {
        try {
            db.close();
        } catch (IOException e) {
            log.error("close ['{}'] -> error", path, e);
        }
    }

    public byte[] get(byte[] key) throws DBException {
        return db.get(key, getReadOptions());
    }

    public DBIterator iterator() {
        return db.iterator(getReadOptions());
    }

    public void put(byte[] key, byte[] value) throws DBException {
        db.put(key, value, getWriteOptions());
    }

    public void delete(byte[] key) throws DBException {
        db.delete(key);
    }

    private DB openDatabase(DBFactory dbFactory, String location, Options options) throws IOException {
        File dir = new File(location);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dbFactory.open(dir, options);
    }

    private ReadOptions getReadOptions() {
        return readOptions;
    }

    private WriteOptions getWriteOptions() {
        return writeOptions;
    }

    private Options dbOptions() {
        return new Options().createIfMissing(true);
    }
}
