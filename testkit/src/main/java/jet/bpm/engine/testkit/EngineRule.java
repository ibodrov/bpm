package jet.bpm.engine.testkit;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultEngine;
import jet.bpm.engine.IndexedProcessDefinition;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.event.InMemEventStorage;
import jet.bpm.engine.leveldb.Configuration;
import jet.bpm.engine.leveldb.KryoSerializer;
import jet.bpm.engine.leveldb.LevelDbPersistenceManager;
import jet.bpm.engine.lock.StripedLockManagerImpl;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.xml.Parser;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class EngineRule implements TestRule {

    private final Parser parser;

    private TestProcessDefinitionProvider processDefinitionProvider;
    private EventPersistenceManager eventManager;
    private Engine engine;

    public EngineRule(Parser parser) {
        this.parser = parser;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(description);
                try {
                    base.evaluate();
                } finally {
                    after(description);
                }
            }
        };
    }

    protected void before(Description description) throws Exception {
        if (engine == null) {
            processDefinitionProvider = new TestProcessDefinitionProvider();
            eventManager = new EventPersistenceManagerImpl(new InMemEventStorage());
            Configuration cfg = new Configuration();
            cfg.setExecutionPath("/tmp/bpm/" + System.currentTimeMillis());
            DBFactory f = new Iq80DBFactory();
            LevelDbPersistenceManager levelDbPersistenceManager = new LevelDbPersistenceManager(cfg, f, new KryoSerializer());
            levelDbPersistenceManager.init();

            engine = new DefaultEngine(processDefinitionProvider, new Mocks.Registry(), eventManager, levelDbPersistenceManager, new StripedLockManagerImpl(1));
        }

        Class<?> k = description.getTestClass();
        String n = description.getMethodName();
        for (Method m : k.getDeclaredMethods()) {
            if (m.getName().equals(n)) {
                Deployment d = m.getAnnotation(Deployment.class);
                if (d != null) {
                    for (String s : d.resources()) {
                        InputStream in = ClassLoader.getSystemResourceAsStream(s);
                        ProcessDefinition pd = parser.parse(in);
                        IndexedProcessDefinition ipd = new IndexedProcessDefinition(pd);
                        processDefinitionProvider.add(ipd);
                    }
                }
            }
        }
    }

    protected void after(Description description) {
        engine = null;
    }

    public String startProcessInstanceByKey(String key, Map<String, Object> input) throws ExecutionException {
        String id = UUID.randomUUID().toString();
        startProcessInstanceByKey(id, key, input);
        return id;
    }

    public void startProcessInstanceByKey(String txId, String key, Map<String, Object> input) throws ExecutionException {
        engine.start(txId, key, input);
    }

    public void wakeUp(String key, String eventId) throws ExecutionException {
        wakeUp(key, eventId, null);
    }

    public void wakeUp(String key, String eventId, Map<String, Object> variables) throws ExecutionException {
        engine.resume(key, eventId, variables);
    }

    public EventPersistenceManager getEventManager() {
        return eventManager;
    }
}
