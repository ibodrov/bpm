package jet.bpm.engine;

import jet.bpm.engine.persistence.DummyPersistenceManager;
import jet.bpm.engine.persistence.PersistenceManager;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.lock.LockManager;
import jet.bpm.engine.el.DefaultExpressionManager;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventPersistenceManagerImpl;
import jet.bpm.engine.event.EventStorage;
import jet.bpm.engine.handlers.DelegatingElementHandler;
import jet.bpm.engine.handlers.ElementHandler;
import jet.bpm.engine.lock.NoopLockManager;
import jet.bpm.engine.task.ServiceTaskRegistry;

public class DefaultEngine extends AbstractEngine implements Engine {

    private final ElementHandler elementHandler = new DelegatingElementHandler(this);
    private final PersistenceManager persistenceManager;
    private final LockManager lockManager;
    private final UuidGenerator idGenerator = new RandomUuidGenerator();

    private final IndexedProcessDefinitionProvider processDefinitionProvider;
    private final ServiceTaskRegistry serviceTaskRegistry;
    private final ExpressionManager expressionManager;
    private final EventPersistenceManager eventManager;

    public DefaultEngine(ProcessDefinitionProvider processDefinitionProvider, ServiceTaskRegistry serviceTaskRegistry, EventStorage eventStorage) {
        this(processDefinitionProvider, serviceTaskRegistry, new EventPersistenceManagerImpl(eventStorage));
    }

    public DefaultEngine(
            ProcessDefinitionProvider processDefinitionProvider,
            ServiceTaskRegistry serviceTaskRegistry,
            EventPersistenceManager eventPersistenceManager) {
        this(processDefinitionProvider, serviceTaskRegistry, eventPersistenceManager, new DummyPersistenceManager(), new NoopLockManager());
    }

    public DefaultEngine(
            ProcessDefinitionProvider processDefinitionProvider,
            ServiceTaskRegistry serviceTaskRegistry,
            EventPersistenceManager eventPersistenceManager,
            PersistenceManager persistenceManager,
            LockManager lockManager) {
        this.processDefinitionProvider = new IndexedProcessDefinitionProvider(processDefinitionProvider);
        this.serviceTaskRegistry = serviceTaskRegistry;
        this.eventManager = eventPersistenceManager;
        this.expressionManager = new DefaultExpressionManager(serviceTaskRegistry);
        this.persistenceManager = persistenceManager;
        this.lockManager = lockManager;
    }

    @Override
    public IndexedProcessDefinitionProvider getProcessDefinitionProvider() {
        return processDefinitionProvider;
    }

    @Override
    public ElementHandler getElementHandler() {
        return elementHandler;
    }

    @Override
    public EventPersistenceManager getEventManager() {
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
    public UuidGenerator getUuidGenerator() {
        return idGenerator;
    }
}
