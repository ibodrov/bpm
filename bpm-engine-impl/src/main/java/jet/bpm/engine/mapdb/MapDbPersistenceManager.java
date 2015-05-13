package jet.bpm.engine.mapdb;

import java.io.File;
import java.util.Map;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.persistence.PersistenceManager;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDbPersistenceManager implements PersistenceManager {
    
    private String baseDir = "/tmp/";
    private DB db;
    private Map<String, DefaultExecution> store;

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public synchronized void start() {
        File f = new File(baseDir);
        f.mkdirs();

        f = new File(baseDir + "/db");
        db = DBMaker.newFileDB(f)
                .transactionDisable()
                .mmapFileEnableIfSupported()
                .asyncWriteEnable()
                .make();

        store = db.getHashMap("executions");
    }

    public synchronized void stop() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    @Override
    public void save(DefaultExecution execution) {
        String id = execution.getId();
        store.put(id, execution);
    }

    @Override
    public DefaultExecution get(String id) {
        return store.get(id);
    }

    @Override
    public DefaultExecution remove(String id) {
        return store.remove(id);        
    }
}
