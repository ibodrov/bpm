package jet.bpm.engine;

import jet.bpm.engine.persistence.DummyPersistenceManager;
import jet.bpm.engine.persistence.PersistenceManager;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.lock.LockManager;
import jet.bpm.engine.el.DefaultExpressionManager;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.event.EventManager;
import jet.bpm.engine.event.EventManagerImpl;
import jet.bpm.engine.handlers.DelegateElementHandler;
import jet.bpm.engine.handlers.ElementHandler;
import jet.bpm.engine.lock.NoopLockManager;
import jet.bpm.engine.task.ServiceTaskRegistry;
import jet.bpm.engine.task.ServiceTaskRegistryImpl;

public class DefaultEngine extends AbstractEngine implements Engine {
    
    private final ElementHandler elementHandler = new DelegateElementHandler(this);
    private final PersistenceManager persistenceManager = new DummyPersistenceManager();
    private final LockManager lockManager = new NoopLockManager();
    private final IdGenerator idGenerator = new UuidGenerator();
    
    private final ProcessDefinitionProvider processDefinitionProvider;
    private final ServiceTaskRegistry serviceTaskRegistry;
    private final ExpressionManager expressionManager;
    private final EventManager eventManager;

    public DefaultEngine() {
        this(new ProcessDefinitionProviderImpl(), new ServiceTaskRegistryImpl(), new EventManagerImpl());
    }
    
    public DefaultEngine(ProcessDefinitionProvider processDefinitionProvider, ServiceTaskRegistry serviceTaskRegistry, EventManager eventManager) {
        this.processDefinitionProvider = processDefinitionProvider;
        this.serviceTaskRegistry = serviceTaskRegistry;
        this.eventManager = eventManager;
        this.expressionManager = new DefaultExpressionManager(serviceTaskRegistry);
    }

    @Override
    public ProcessDefinitionProvider getProcessDefinitionProvider() {
        return processDefinitionProvider;
    }

    @Override
    public ElementHandler getElementHandler() {
        return elementHandler;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    @Override
    public ServiceTaskRegistry getServiceTaskRegistry() {
        return serviceTaskRegistry;
    }

    @Override
    public ExpressionManager getExpressionManager() {
        return expressionManager;
    }

    @Override
    public LockManager getLockManager() {
        return lockManager;
    }

    @Override
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }
}
