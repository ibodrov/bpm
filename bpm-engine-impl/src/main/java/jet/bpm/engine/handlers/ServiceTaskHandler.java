package jet.bpm.engine.handlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.el.ELException;
import jet.bpm.engine.api.BpmnError;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.BpmnErrorHelper;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.task.ServiceTaskRegistry;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.ExpressionType;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service task handling. Supports task calling by delegate expression - EL
 * expression which evals into {@link JavaDelegate} instance.
 *
 * @see ServiceTaskRegistry
 */
public class ServiceTaskHandler extends AbstractElementHandler {

    private static final Logger log = LoggerFactory.getLogger(ServiceTaskHandler.class);
    private static final long MIN_TIMER_FREQUENCY = 1000;

    private final ExecutorService timerExecutor = Executors.newCachedThreadPool();

    public ServiceTaskHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        ServiceTask t = (ServiceTask) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        // check if there is any timers associated with the task
        List<TimerDefinition> timers = findTimers(pd, c);

        ExpressionType type = t.getType();
        String expr = t.getExpression();

        if (expr != null) {
            ExpressionManager em = getEngine().getExpressionManager();
            ExecutionContext ctx = s.getContext();

            Invoker invoker;
            if (timers.isEmpty()) {
                invoker = new DirectInvoker(em, type, expr, ctx, c.getElementId());
            } else {
                invoker = new TimerInvoker(em, type, expr, ctx, c.getElementId(), timerExecutor, timers);
            }
            
            try {
                String nextId = invoker.invoke();
                FlowUtils.followFlows(getEngine(), s, c, nextId);
            } catch (ELException e) {
                Throwable cause = e.getCause();
                if (cause instanceof BpmnError) {
                    handleBpmError(s, pd, c, (BpmnError) cause);
                } else {
                    throw e;
                }
            } catch (BpmnError e) {
                handleBpmError(s, pd, c, e);
            } catch (ExecutionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExecutionException("Unhandled execution exception: " + expr, e);
            }
        } else {
            log.debug("handle ['{}', '{}', '{}'] -> empty expression, noop", s.getBusinessKey(), c.getElementId(), expr);
            FlowUtils.followFlows(getEngine(), s, c);
        }
    }

    /**
     * BPMN error handling. Unlike common exceptions, error references is used.
     * Handles error boundary events.
     *
     * @param s current execution.
     * @param pd current process definition.
     * @param c current process command.
     * @param e handled error.
     * @throws ExecutionException
     */
    private void handleBpmError(DefaultExecution s, ProcessDefinition pd, ProcessElementCommand c, BpmnError e) throws ExecutionException {
        String errorRef = e.getErrorRef();
        String bk = s.getBusinessKey();
        String eid = c.getElementId();

        BoundaryEvent ev = ProcessDefinitionUtils.findBoundaryEvent(pd, eid, errorRef);
        if (ev == null) {
            ev = ProcessDefinitionUtils.findBoundaryEvent(pd, eid, null);
        }

        if (ev != null) {
            // the task element has an boundary error event - the process
            // execution will follow its flow
            log.debug("handleBpmError ['{}', '{}'] -> handle boundary error '{}'", bk, eid, errorRef);
            // save errorRef for later
            s.getContext().setVariable(ExecutionContext.ERROR_CODE_KEY, errorRef);
            FlowUtils.followFlows(getEngine(), s, c, ev.getId());
        } else {
            // no boundary error events were found - an error will be raised to
            // the parent execution
            log.debug("handleBpmError ['{}', '{}'] -> no boundary error", bk, eid, errorRef);
            BpmnErrorHelper.raiseError(s.getContext(), errorRef);
        }
    }

    private List<TimerDefinition> findTimers(ProcessDefinition pd, ProcessElementCommand c) throws ExecutionException {
        String eid = c.getElementId();

        List<BoundaryEvent> events = ProcessDefinitionUtils.findOptionalBoundaryEvents(pd, eid);
        List<TimerDefinition> l = new ArrayList<>(events.size());
        for (BoundaryEvent ev : events) {
            if (ev.getTimeDuration() != null) {
                Duration d = Duration.parse(ev.getTimeDuration());
                l.add(new TimerDefinition(ev.getId(), d.getMillis()));
            }
        }

        Collections.sort(l, new Comparator<TimerDefinition>() {
            @Override
            public int compare(TimerDefinition o1, TimerDefinition o2) {
                return (int) (o1.duration - o2.duration);
            }
        });

        return l;
    }

    private static class TimerDefinition implements Serializable {

        private final String timerId;
        private final long duration;

        public TimerDefinition(String timerId, long duration) {
            this.timerId = timerId;
            this.duration = duration;
        }
    }

    private static abstract class Invoker {

        private final ExpressionManager em;
        private final ExpressionType type;
        private final String expr;
        private final ExecutionContext ctx;
        private final String nextElementId;

        public Invoker(ExpressionManager em, ExpressionType type, String expr, ExecutionContext ctx, String nextElementId) {
            this.em = em;
            this.type = type;
            this.expr = expr;
            this.ctx = ctx;
            this.nextElementId = nextElementId;
        }

        public String invoke() throws Exception {
            Callable<?> c = null;

            switch (type) {
                case SIMPLE: {
                    c = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            em.eval(ctx, expr, Object.class);
                            return null;
                        }
                    };
                    break;
                }

                case DELEGATE: {
                    c = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            JavaDelegate d = em.eval(ctx, expr, JavaDelegate.class);
                            d.execute(ctx);
                            return null;
                        }
                    };
                    break;
                }

                case NONE: {
                    // NOOP
                    break;
                }

                default: {
                    throw new ExecutionException("Unsupported expression type '%s'", type);
                }
            }

            if (c != null) {
                String id = doCall(c);
                return id != null ? id : nextElementId;
            }
            
            return nextElementId;
        }

        protected abstract String doCall(Callable<?> c) throws Exception;
        
        protected String getNextElementId() {
            return nextElementId;
        }
    }

    private static class DirectInvoker extends Invoker {

        public DirectInvoker(ExpressionManager em, ExpressionType type, String expr, ExecutionContext ctx, String nextElementId) {
            super(em, type, expr, ctx, nextElementId);
        }
        
        @Override
        protected String doCall(Callable<?> c) throws Exception {
            c.call();
            return getNextElementId();
        }
    }

    private static class TimerInvoker extends Invoker {
        
        private final ExecutorService executor;
        private final List<TimerDefinition> timers;

        public TimerInvoker(ExpressionManager em, ExpressionType type, String expr, ExecutionContext ctx, String nextElementId, ExecutorService executor, List<TimerDefinition> timers) {
            super(em, type, expr, ctx, nextElementId);
            this.executor = executor;
            this.timers = timers;
        }

        @Override
        protected String doCall(Callable<?> c) throws Exception {
            long now = System.currentTimeMillis();
            Future<?> f = executor.submit(c);
            
            // poll until the task completion
            while (true) {
                try {
                    f.get(MIN_TIMER_FREQUENCY, TimeUnit.MILLISECONDS);
                    // task is finished, proceed as usual
                    return getNextElementId();
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof BpmnError) {
                        throw (BpmnError) cause;
                    }
                } catch (TimeoutException e) {
                    long now2 = System.currentTimeMillis();
                    for (TimerDefinition d : timers) {
                        long dt = now2 - now;
                        if (d.duration <= dt) {
                            // cancel the running task (at least attempts to do
                            // it)
                            f.cancel(true);

                            // execute the timer's flow
                            return d.timerId;
                        }
                    }
                }
            }
        }
    }
}
