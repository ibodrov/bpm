package jet.bpm.benchmark;

import java.io.InputStream;
import java.util.UUID;
import jet.bpm.engine.DefaultEngine;
import jet.bpm.engine.ProcessDefinitionProviderImpl;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.leveldb.Configuration;
import jet.bpm.engine.leveldb.KryoSerializer;
import jet.bpm.engine.leveldb.LevelDbEventStorage;
import jet.bpm.engine.lock.LockManager;
import jet.bpm.engine.lock.StripedLockManagerImpl;
import jet.bpm.engine.mapdb.MapDbPersistenceManager;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.task.ServiceTaskRegistryImpl;
import jet.bpm.engine.xml.ParserException;
import jet.bpm.engine.xml.activiti.ActivitiParser;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class SimpleBenchmark {

    public static void main(String[] args) throws Exception {
//        benchmark("chain-of-tasks.bpmn");
//        benchmark("exclusive-gw.bpmn");
        benchmark("messages.bpmn", "a", "c");
    }

    private static void benchmark(String path, String ... events) throws Exception {
        System.out.println("=== " + path + " ===");

        // ---

        ProcessDefinitionProviderImpl pdp = new ProcessDefinitionProviderImpl();
        pdp.add(parse(path));

        ServiceTaskRegistryImpl str = new ServiceTaskRegistryImpl();
        str.register("t", new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws Exception {
            }
        });

        String baseDir = "/tmp/test#" + System.currentTimeMillis();
        System.out.println("<< " + baseDir);
        Configuration ldbCfg = new Configuration();
        ldbCfg.setSyncWrite(false);
        ldbCfg.setBusinessKeyEventIndexPath(baseDir + "/bkindex");
        ldbCfg.setEventPath(baseDir + "/events");
        ldbCfg.setExecutionPath(baseDir + "/executions");
        ldbCfg.setExpiredEventIndexPath(baseDir + "/expired");

        DBFactory dbf = new Iq80DBFactory();
        KryoSerializer kryo = new KryoSerializer();
        LevelDbEventStorage es = new LevelDbEventStorage(ldbCfg, dbf, kryo);
        es.init();

        EventPersistenceManager epm = new EventPersistenceManagerImpl(es);
        MapDbPersistenceManager pm = new MapDbPersistenceManager();
        pm.setBaseDir(baseDir + "/mapdb");
        pm.start();

        LockManager lm = new StripedLockManagerImpl(32);

        Engine engine = new DefaultEngine(pdp, str, epm, pm, lm);

        // ---

        int tests = 10;
        int iterations = 100000;

        for (int t = 0; t < tests; t++) {
            long time = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                String key = UUID.randomUUID().toString();
                engine.start(key, "myProcess", null);

                if (events != null) {
                    for (String e : events) {
                        engine.resume(key, e, null);
                    }
                }
            }
            long now = System.currentTimeMillis();
            System.out.println("test #" + t + ", " + iterations + " iteration(s), took " + (now - time) + "ms");
        }

        pm.stop();
        es.close();
    }

    private static ProcessDefinition parse(String path) throws ParserException {
        InputStream in = ClassLoader.getSystemResourceAsStream(path);
        ActivitiParser parser = new ActivitiParser();
        return parser.parse(in);
    }
}
