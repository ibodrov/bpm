package jet.scdp.bpm.engine;

import jet.scdp.bpm.engine.persistence.PersistenceManagerImpl;
import jet.scdp.bpm.engine.persistence.PersistenceManager;
import jet.scdp.bpm.api.Engine;
import jet.scdp.bpm.engine.lock.LockManager;
import jet.scdp.bpm.engine.el.DefaultExpressionManager;
import jet.scdp.bpm.engine.el.ExpressionManager;
import jet.scdp.bpm.engine.event.EventManager;
import jet.scdp.bpm.engine.event.EventManagerImpl;
import jet.scdp.bpm.engine.handlers.DelegateElementHandler;
import jet.scdp.bpm.engine.handlers.ElementHandler;
import jet.scdp.bpm.engine.lock.NoopLockManager;
import jet.scdp.bpm.engine.task.ServiceTaskRegistry;
import jet.scdp.bpm.engine.task.ServiceTaskRegistryImpl;

public class DefaultEngine extends AbstractEngine implements Engine {
    
    private final ElementHandler elementHandler = new DelegateElementHandler(this);
    private final PersistenceManager persistenceManager = new PersistenceManagerImpl();
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
