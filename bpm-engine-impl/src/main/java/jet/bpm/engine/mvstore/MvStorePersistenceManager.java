package jet.bpm.engine.mvstore;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.persistence.PersistenceManager;
import org.h2.mvstore.MVStore;

public class MvStorePersistenceManager implements PersistenceManager {

    private String baseDir;
    private MVStore store;
    private Map<UUID, DefaultExecution> map;
    
    public void start() {
        File f = new File(baseDir);
        f.mkdirs();
        f = new File(f, "store");
        
        store = new MVStore.Builder()
                .fileName(f.getAbsolutePath())
                .open();
        map = store.openMap("executions");
    }
    
    public void stop() {
        store.close();
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    @Override
    public void save(DefaultExecution execution) {
        map.put(execution.getId(), execution);
    }

    @Override
    public DefaultExecution get(UUID id) {
        return map.get(id);
    }

    @Override
    public DefaultExecution remove(UUID id) {
        return map.remove(id);
    }
    
}
