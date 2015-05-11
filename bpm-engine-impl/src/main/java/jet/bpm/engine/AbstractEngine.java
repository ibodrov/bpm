package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ActivationListener;
import java.util.ArrayList;
import java.util.List;
import jet.bpm.engine.persistence.PersistenceManager;
import jet.bpm.engine.api.Engine;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.api.ExecutionException;
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

    public abstract ProcessDefinitionProvider getProcessDefinitionProvider();

    public abstract ElementHandler getElementHandler();

    public abstract EventPersistenceManager getEventManager();

    public abstract PersistenceManager getPersistenceManager();

    public abstract ServiceTaskRegistry getServiceTaskRegistry();

    public abstract ExpressionManager getExpressionManager();

    public abstract LockManager getLockManager();

    public abstract IdGenerator getIdGenerator();

    @Override
    public void addListener(ActivationListener l) {
        listenerHolder.addListener(l);
    }

    public void fireOnElementActivation(DefaultExecution e, String processDefinitionId, String elementId) {
        listenerHolder.fireOnElementActivation(e, processDefinitionId, elementId);
    }

    @Override
    public void start(String processBusinessKey, String processDefinitionId, Map<String, Object> variables) throws ExecutionException {
        ProcessDefinitionProvider pdp = getProcessDefinitionProvider();

        ProcessDefinition pd = pdp.getById(processDefinitionId);
        StartEvent start = ProcessDefinitionUtils.findStartEvent(pd);

        ExecutionContext ctx = new ExecutionContextImpl();
        applyVariables(ctx, variables);

        IdGenerator idg = getIdGenerator();

        DefaultExecution s = new DefaultExecution(idg.create(), processBusinessKey);
        s.push(new ProcessElementCommand(processDefinitionId, start.getId(), ctx));

        LockManager lm = getLockManager();
        lm.lock(processBusinessKey);
        try {
            run(s);
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
            Event e = em.remove(processBusinessKey, eventName);
            if (e == null) {
                throw new ExecutionException("No event '%s' found for process '%s'", eventName, processBusinessKey);
            }

            if (e.isExclusive()) {
                // exclusive event means that only one event from the group of
                // events can happen. Rest of events must be removed.
                em.clearGroup(processBusinessKey, e.getGroupId());
            }

            String eid = e.getExecutionId();
            log.debug("resume ['{}', '{}'] -> got execution id {}", processBusinessKey, eventName, eid);

            PersistenceManager pm = getPersistenceManager();
            DefaultExecution s = pm.remove(eid);
            if (s == null) {
                throw new ExecutionException("No execution '%s' found for process '%s'", eid, processBusinessKey);
            }

            s.setSuspended(false);

            ExecutionCommand c = s.peek();
            applyVariables(c.getContext(), variables);

            run(s);
        } finally {
            lm.unlock(processBusinessKey);
        }
    }

    private void applyVariables(ExecutionContext ctx, Map<String, Object> m) {
        if (m == null) {
            return;
        }

        for (Map.Entry<String, Object> e : m.entrySet()) {
            ctx.setVariable(e.getKey(), e.getValue());
        }
    }

    private void run(DefaultExecution s) throws ExecutionException {
        PersistenceManager pm = getPersistenceManager();

        while (!s.isSuspended()) {
            if (s.isDone()) {
                String pid = s.getParentId();
                if (pid == null) {
                    log.debug("run ['{}'] -> no parent execution, breaking", s.getId());
                    break;
                } else {
                    log.debug("run ['{}'] -> switching to {}", s.getId(), pid);
                    DefaultExecution parent = pm.remove(pid);
                    if (parent == null) {
                        // this is typical for the scenarios where the parent
                        // process ends before its children (e.g. "Inclusive
                        // Gateway").
                        log.debug("run ['{}'] -> parent execution not found", pid);
                        break;
                    } else {
                        parent.setSuspended(false);
                        s = parent;
                    }
                }
            }

            ExecutionCommand c = s.peek();
            if (c != null) {
                s = c.exec(this, s);
            }
        }

        log.debug("run ['{}'] -> (done: {}, suspended: {})", s.getId(), s.isDone(), s.isSuspended());

    }

    private static final class ActivationListenerHolder {

        private final List<ActivationListener> listeners = new ArrayList<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        public void addListener(ActivationListener l) {
            try {
                lock.writeLock().lock();
                listeners.add(l);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void fireOnElementActivation(Execution e, String processDefinitionId, String elementId) {
            try {
                lock.readLock().lock();
                for (ActivationListener l : listeners) {
                    l.onActivation(e, processDefinitionId, elementId);
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }
}
