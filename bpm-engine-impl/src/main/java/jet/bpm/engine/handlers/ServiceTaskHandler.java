package jet.bpm.engine.handlers;

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

    public ServiceTaskHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        ServiceTask t = (ServiceTask) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        ExpressionType type = t.getType();
        String expr = t.getExpression();

        if (expr != null) {
            ExpressionManager em = getEngine().getExpressionManager();
            ExecutionContext ctx = s.getContext();

            try {
                switch (type) {
                    case SIMPLE: {
                        // simple case: task is just an expression
                        em.eval(ctx, expr, Object.class);
                        break;
                    }
                    case DELEGATE: {
                        // delegation: task is an expression that evals into a
                        // delegate
                        JavaDelegate d = em.eval(ctx, expr, JavaDelegate.class);
                        d.execute(ctx);
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
                FlowUtils.followFlows(getEngine(), s, c);
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
}
