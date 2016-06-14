package jet.bpm.benchmark;

import com.google.common.io.Files;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jet.bpm.engine.DefaultEngine;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.event.InMemEventStorage;
import jet.bpm.engine.leveldb.Configuration;
import jet.bpm.engine.leveldb.KryoSerializer;
import jet.bpm.engine.leveldb.LevelDbEventStorage;
import jet.bpm.engine.leveldb.Serializer;
import jet.bpm.engine.lock.LockManager;
import jet.bpm.engine.lock.StripedLockManagerImpl;
import jet.bpm.engine.mapdb.MapDbPersistenceManager;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.persistence.MapPersistenceManager;
import jet.bpm.engine.persistence.PersistenceManager;
import jet.bpm.engine.task.ServiceTaskRegistry;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.openjdk.jmh.annotations.TearDown;

public abstract class AbstractBenchmarkState {
    
    private final Engine engine;
    private final DummyServiceTaskRegistry serviceTaskRegistry;
    private LevelDbEventStorage levelDbEventStorage;
    private MapDbPersistenceManager mapDbPersistenceManager;
    
    public AbstractBenchmarkState(ProcessDefinition def) {
        this(true, def);
    }
    
    public AbstractBenchmarkState(boolean inMem, ProcessDefinition def) {
        this.serviceTaskRegistry = new DummyServiceTaskRegistry();
        
        DummyProcessDefinitionProvider defs = new DummyProcessDefinitionProvider();
        defs.publish(def.getId(), def);
        
        LockManager lockManager = new StripedLockManagerImpl(65536);
        EventPersistenceManager eventPersistenceManager;
        PersistenceManager persistenceManager;
        
        if (inMem) {
            eventPersistenceManager = new EventPersistenceManagerImpl(new InMemEventStorage());
            persistenceManager = new MapPersistenceManager(new ConcurrentHashMap<>());
        } else {
            String baseDir;
            try {
                File f = Files.createTempDir();
                f.mkdirs();
                baseDir = f.getAbsolutePath();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            Configuration cfg = new Configuration();
            cfg.setEventPath(baseDir + "/events");
            cfg.setExecutionPath(baseDir + "/executions");
            cfg.setExpiredEventIndexPath(baseDir + "/exprired");
            cfg.setBusinessKeyEventIndexPath(baseDir + "/bki");
            
            DBFactory dbf = new Iq80DBFactory();
            
            Serializer serializer = new KryoSerializer();
            
            levelDbEventStorage = new LevelDbEventStorage(cfg, dbf, serializer);
            levelDbEventStorage.init();
            eventPersistenceManager = new EventPersistenceManagerImpl(levelDbEventStorage);
            
            mapDbPersistenceManager = new MapDbPersistenceManager();
            mapDbPersistenceManager.setBaseDir(baseDir + "/executions");
            mapDbPersistenceManager.start();
            persistenceManager = mapDbPersistenceManager;
        }
        
        this.engine = new DefaultEngine(defs, serviceTaskRegistry, eventPersistenceManager, persistenceManager, lockManager);
    }
    
    @TearDown
    public void close() {
        if (levelDbEventStorage != null) {
            levelDbEventStorage.close();
        }
        
        if (mapDbPersistenceManager != null) {
            mapDbPersistenceManager.stop();
        }
    }

    public final Engine getEngine() {
        return engine;
    }

    public final DummyServiceTaskRegistry getServiceTaskRegistry() {
        return serviceTaskRegistry;
    }
    
    public static class DummyProcessDefinitionProvider implements ProcessDefinitionProvider {

        private final Map<String, ProcessDefinition> defs = new HashMap<>();

        public void publish(String id, ProcessDefinition def) {
            defs.put(id, def);
        }

        @Override
        public ProcessDefinition getById(String id) throws ExecutionException {
            return defs.get(id);
        }
    }

    public static class DummyServiceTaskRegistry implements ServiceTaskRegistry {

        private final Map<String, Object> tasks = new HashMap<>();
        
        public void register(String key, Object instance) {
            tasks.put(key, instance);
        }

        @Override
        public Object getByKey(String key) {
            return tasks.get(key);
        }
    }    
}
