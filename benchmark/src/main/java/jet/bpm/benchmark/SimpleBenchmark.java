package jet.bpm.benchmark;

import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;
import jet.bpm.engine.DefaultEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ProcessDefinitionProviderImpl;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.event.InMemEventStorage;
import jet.bpm.engine.lock.LockManager;
import jet.bpm.engine.lock.StripedLockManagerImpl;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.persistence.MapPersistenceManager;
import jet.bpm.engine.persistence.PersistenceManager;
import jet.bpm.engine.task.ServiceTaskRegistryImpl;
import jet.bpm.engine.xml.ParserException;
import jet.bpm.engine.xml.activiti.ActivitiParser;

public class SimpleBenchmark {

    public static void main(String[] args) throws Exception {
        benchmark("chain-of-tasks.bpmn");
        benchmark("exclusive-gw.bpmn");
    }

    private static void benchmark(String path) throws Exception {
        ProcessDefinitionProviderImpl pdp = new ProcessDefinitionProviderImpl();
        pdp.add(parse(path));

        ServiceTaskRegistryImpl str = new ServiceTaskRegistryImpl();
        str.register("t", new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws Exception {
            }
        });

        EventPersistenceManager epm = new EventPersistenceManagerImpl(new InMemEventStorage());
        PersistenceManager pm = new MapPersistenceManager(new HashMap<String, DefaultExecution>());
        LockManager lm = new StripedLockManagerImpl(32);

        Engine engine = new DefaultEngine(pdp, str, epm, pm, lm);

        // ---

        int tests = 10;
        int iterations = 1_000_00;

        for (int t = 0; t < tests; t++) {
            long time = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                String key = UUID.randomUUID().toString();
                engine.start(key, "myProcess", null);
            }
            long now = System.currentTimeMillis();
            System.out.println("test #" + t + ", took " + (now - time) + "ms");
        }
    }

    private static ProcessDefinition parse(String path) throws ParserException {
        InputStream in = ClassLoader.getSystemResourceAsStream(path);
        ActivitiParser parser = new ActivitiParser();
        return parser.parse(in);
    }
}
