package jet.bpm.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.event.InMemEventStorage;
import jet.bpm.engine.task.ServiceTaskRegistryImpl;
import jet.bpm.engine.model.ProcessDefinition;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEngineTest implements ActivationListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractEngineTest.class);

    private TestProcessDefinitionProvider processDefinitionProvider;
    private ServiceTaskRegistryImpl serviceTaskRegistry;
    protected EventPersistenceManager eventManager;
    private AbstractEngine engine;
    private Map<String, List<String>> activations;

    @Before
    public void init() {
        processDefinitionProvider = new TestProcessDefinitionProvider();
        serviceTaskRegistry = new ServiceTaskRegistryImpl();
        eventManager = spy(new EventPersistenceManagerImpl(new InMemEventStorage()));

        engine = new DefaultEngine(processDefinitionProvider, serviceTaskRegistry, eventManager);

        activations = new HashMap<>();
        engine.addListener(this);
    }

    protected ServiceTaskRegistryImpl getServiceTaskRegistry() {
        return serviceTaskRegistry;
    }
    
    protected void deploy(ProcessDefinition pd) {
        IndexedProcessDefinition ipd = new IndexedProcessDefinition(pd);
        processDefinitionProvider.add(ipd);
    }

    protected AbstractEngine getEngine() {
        return engine;
    }

    protected void register(String key, JavaDelegate d) {
        serviceTaskRegistry.register(key, d);
    }

    @Override
    public void onActivation(Execution e, String processDefinitionId, String elementId) {
        String k = e.getBusinessKey() + "/" + processDefinitionId;
        List<String> l = activations.get(k);
        if (l == null) {
            l = new ArrayList<>();
            activations.put(k, l);
        }

        l.add(elementId);
    }

    protected void assertActivation(String processBusinessKey, String processDefinitionId, String elementId) {
        String k = processBusinessKey + "/" + processDefinitionId;
        List<String> l = activations.get(k);
        assertNotNull("No activations for " + k, l);
        assertFalse("No more activations for " + k + ", element " + elementId, l.isEmpty());

        String s = l.remove(0);
        assertTrue("Unexpected activation: '" + s + "' instead of '" + elementId + "'", elementId.equals(s));
    }

    protected void assertActivations(String processBusinessKey, String processDefinitionId, String ... elementIds) {
        for (String eid : elementIds) {
            assertActivation(processBusinessKey, processDefinitionId, eid);
        }
    }

    protected void assertNoMoreActivations() {
        StringBuilder b = new StringBuilder();
        int s = 0;
        for (List<String> l : activations.values()) {
            s += l.size();
        }
        assertTrue("We have " + s + " more activations", s == 0);
    }

    protected void dumpActivations(String processBusinessKey, String processDefinitionId) {
        String k = processBusinessKey + "/" + processDefinitionId;
        List<String> l = activations.get(k);
        log.info("dumpActivations ['{}', '{}'] -> done: {}", processBusinessKey, processDefinitionId,
                Arrays.asList(l.toArray(new String[l.size()])));
    }
}
