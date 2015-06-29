package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ActivationListener;
import java.util.Collection;
import java.util.List;
import jet.bpm.engine.persistence.PersistenceManager;
import jet.bpm.engine.api.Engine;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.api.NoEventFoundException;
import jet.bpm.engine.commands.ExecutionCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.handlers.ElementHandler;
import jet.bpm.engine.lock.LockManager;
import jet.bpm.engine.task.ServiceTaskRegistry;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.StartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEngine implements Engine {

    private static final Logger log = LoggerFactory.getLogger(AbstractEngine.class);

    private final ActivationListenerHolder listenerHolder = new ActivationListenerHolder();

    public abstract IndexedProcessDefinitionProvider getProcessDefinitionProvider();

    public abstract ElementHandler getElementHandler();

    public abstract EventPersistenceManager getEventManager();

    public abstract PersistenceManager getPersistenceManager();

    public abstract ServiceTaskRegistry getServiceTaskRegistry();

    public abstract ExpressionManager getExpressionManager();

    public abstract LockManager getLockManager();

    public abstract UuidGenerator getUuidGenerator();

    @Override
    public void addListener(ActivationListener l) {
        listenerHolder.addListener(l);
    }

    public void fireOnElementActivation(DefaultExecution e, String processDefinitionId, String elementId) {
        listenerHolder.fireOnElementActivation(e, processDefinitionId, elementId);
    }

    @Override
    public void start(String processBusinessKey, String processDefinitionId, Map<String, Object> variables) throws ExecutionException {
        IndexedProcessDefinitionProvider pdp = getProcessDefinitionProvider();

        ProcessDefinition pd = pdp.getById(processDefinitionId);
        StartEvent start = ProcessDefinitionUtils.findStartEvent(pd);

        ExecutionContext ctx = new ExecutionContextImpl(null);
        applyVariables(ctx, variables);

        UuidGenerator idg = getUuidGenerator();

        DefaultExecution s = new DefaultExecution(idg.generate(), processBusinessKey, ctx);
        s.push(new ProcessElementCommand(processDefinitionId, start.getId()));

        LockManager lm = getLockManager();
        lm.lock(processBusinessKey);
        try {
            runLockSafe(s);
        } finally {
            lm.unlock(processBusinessKey);
        }
    }

    @Override
    public void resume(String processBusinessKey, String eventName, Map<String, Object> variables) throws ExecutionException {
        LockManager lm = getLockManager();
        lm.lock(processBusinessKey);
        try {
            EventPersistenceManager em = getEventManager();
            Collection<Event> evs = em.find(processBusinessKey, eventName);
            if (evs == null || evs.isEmpty()) {
                throw new NoEventFoundException("No event '%s' found for process '%s'", eventName, processBusinessKey);
            } else if (evs.size() > 1) {
                throw new ExecutionException("Non-unique event name in process '%s': %s", processBusinessKey, eventName);
            }

            Event e = evs.iterator().next();
            resumeLockSafe(e, variables);
        } finally {
            lm.unlock(processBusinessKey);
        }
    }

    @Override
    public void resume(UUID eventId, Map<String, Object> variables) throws ExecutionException {
        EventPersistenceManager em = getEventManager();
        Event e = em.get(eventId);
        if (e == null) {
            throw new NoEventFoundException("No event '%s' found", eventId);
        }

        String processBusinessKey = e.getProcessBusinessKey();

        LockManager lm = getLockManager();
        lm.lock(processBusinessKey);
        try {
            resumeLockSafe(e, variables);
        } finally {
            lm.unlock(processBusinessKey);
        }
    }
    
    public void resume(Event e, Map<String, Object> variables) throws ExecutionException {
        String processBusinessKey = e.getProcessBusinessKey();

        LockManager lm = getLockManager();
        lm.lock(processBusinessKey);        
        try {
            resumeLockSafe(e, variables);
        } finally {
            lm.unlock(processBusinessKey);
        }
    }

    private void resumeLockSafe(Event e, Map<String, Object> variables) throws ExecutionException {
        String processBusinessKey = e.getProcessBusinessKey();
        String eventName = e.getName();

        EventPersistenceManager em = getEventManager();
        if (e.isExclusive()) {
            // exclusive event means that only one event from the group of
            // events can happen. Rest of events must be removed.
            em.clearGroup(processBusinessKey, e.getGroupId());
        } else {
            em.remove(e.getId());
        }

        UUID eid = e.getExecutionId();
        log.debug("resumeLockSafe ['{}', '{}'] -> got '{}'", processBusinessKey, eventName, eid);

        // load execution
        PersistenceManager pm = getPersistenceManager();
        DefaultExecution s = pm.get(eid);
        if (s == null) {
            throw new ExecutionException("No execution '%s' found for process '%s'", eid, processBusinessKey);
        }

        // enable loaded execution
        s.setSuspended(false);

        applyVariables(s.getContext(), variables);

        // process event-to-command mappings (e.g. add next command of the flow
        // to the stack)
        if (!EventMapHelper.isEmpty(s)) {
            EventMapHelper.pushCommands(s, e.getId());
            if (e.isExclusive()) {
                EventMapHelper.clearGroup(s, e.getGroupId());
            }
        } else if (s.isDone()) {
            throw new ExecutionException("No event mapping found in process '%s' or no commands in execution", eid);
        }

        runLockSafe(s);
    }

    private void applyVariables(ExecutionContext ctx, Map<String, Object> m) {
        if (m == null) {
            return;
        }

        for (Map.Entry<String, Object> e : m.entrySet()) {
            ctx.setVariable(e.getKey(), e.getValue());
        }
    }

    private void runLockSafe(DefaultExecution s) throws ExecutionException {
        PersistenceManager pm = getPersistenceManager();

        while (!s.isSuspended()) {
            if (s.isDone()) {
                // check if no more events want this execution
                if (EventMapHelper.isEmpty(s)) {
                    pm.remove(s.getId());
                    log.debug("runLockSafe ['{}'] -> execution removed", s.getId());
                }
                
                break;
            }

            ExecutionCommand c = s.peek();
            if (c != null) {
                s = c.exec(this, s);
            }
        }

        log.debug("runLockSafe ['{}'] -> (done: {}, suspended: {})", s.getId(), s.isDone(), s.isSuspended());
    }

    private static final class ActivationListenerHolder {

        private final List<ActivationListener> listeners = new CopyOnWriteArrayList<>();
        
        public void addListener(ActivationListener l) {
            listeners.add(l);
        }

        public void fireOnElementActivation(Execution e, String processDefinitionId, String elementId) {
            for (ActivationListener l : listeners) {
                l.onActivation(e, processDefinitionId, elementId);
            }
        }
    }
}
