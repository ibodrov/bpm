package jet.scdp.bpm.engine.testkit;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import jet.scdp.bpm.engine.ProcessDefinitionProviderImpl;
import jet.scdp.bpm.api.Engine;
import jet.scdp.bpm.engine.DefaultEngine;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.event.EventManager;
import jet.scdp.bpm.engine.event.EventManagerImpl;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.xml.ActivitiParser;
import jet.scdp.bpm.xml.Parser;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineRule implements TestRule {
    
    private static final Logger log = LoggerFactory.getLogger(EngineRule.class);
    
    private final Parser parser = new ActivitiParser();
    
    private ProcessDefinitionProviderImpl processDefinitionProvider;
    private EventManager eventManager;
    private Engine engine;

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
            processDefinitionProvider = new ProcessDefinitionProviderImpl();
            eventManager = new EventManagerImpl();
            engine = new DefaultEngine(processDefinitionProvider, new Mocks.Registry(), eventManager);
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
                        processDefinitionProvider.add(pd);
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
        engine.run(txId, key, input);
    }
    
    public void wakeUp(String key, String eventId) throws ExecutionException {
        wakeUp(key, eventId, null);
    }
    
    public void wakeUp(String key, String eventId, Map<String, Object> variables) throws ExecutionException {
        engine.resume(key, eventId, variables);
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
