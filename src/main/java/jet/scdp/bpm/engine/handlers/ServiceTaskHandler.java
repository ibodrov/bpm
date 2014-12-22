package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.api.BpmnError;
import jet.scdp.bpm.engine.BpmnErrorHelper;
import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionContext;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.FlowUtils;
import jet.scdp.bpm.engine.task.JavaDelegate;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.engine.el.ExpressionManager;
import jet.scdp.bpm.engine.task.ServiceTaskRegistry;
import jet.scdp.bpm.model.BoundaryEvent;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.ServiceTask;
import jet.scdp.bpm.model.ExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Обработчик элемента 'service task'. Поддерживает вызов задач с помощью т.н.
 * delegate expression - EL-выражения, вычисляемого в тот или иной экземпляр
 * {@link JavaDelegate}.
 *
 * @see ServiceTaskRegistry
 */
public class ServiceTaskHandler extends AbstractElementHandler {

    private static final Logger log = LoggerFactory.getLogger(ServiceTaskHandler.class);

    public ServiceTaskHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(Execution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        ServiceTask t = (ServiceTask) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        ExpressionType type = t.getType();
        String expr = t.getExpression();

        if (expr != null) {
            ExpressionManager em = getEngine().getExpressionManager();
            ExecutionContext ctx = c.getContext();

            try {
                switch (type) {
                    case SIMPLE: {
                        // простой случай: выполнение task - это вычисление EL
                        em.eval(ctx, expr, Object.class);
                        break;
                    }
                    case DELEGATE: {
                        // чуть более сложный случай: выполнение task - это
                        // вызов JavaDelegate полученного при вычислении EL
                        JavaDelegate d = em.eval(ctx, expr, JavaDelegate.class);
                        d.execute(ctx);
                        break;
                    }
                    case NONE: {
                        // NOOP
                        break;
                    }
                    default: {
                        throw new ExecutionException("Unsupported expression type: " + type);
                    }
                }
                FlowUtils.followFlows(getEngine(), s, c);
            } catch (BpmnError e) {
                handleBpmError(s, pd, c, e);
            } catch (ExecutionException e) {
                throw e;
            } catch (Exception e) {
                throw new ExecutionException("Unhandled execution exception: " + expr, e);
            }
        } else {
            log.debug("handle ['{}', '{}', '{}'] -> empty expression, noop", s.getProcessBusinessKey(), c.getElementId(), expr);
            FlowUtils.followFlows(getEngine(), s, c);
        }
    }

    /**
     * Обработка ошибок BPMN. В отличии от обычных исключений, передается
     * идентификатор ошибки. В случае, если на task был привязан "boundary error
     * event", то необходимо его учесть в обработке.
     * @param s текущий процесс.
     * @param pd описание текущего процесса.
     * @param c обрабатываемый элемент (task).
     * @param e обрабатываемая ошибка.
     * @throws ExecutionException
     */
    private void handleBpmError(Execution s, ProcessDefinition pd, ProcessElementCommand c, BpmnError e) throws ExecutionException {
        String errorRef = e.getErrorRef();
        String bk = s.getProcessBusinessKey();
        String eid = c.getElementId();

        BoundaryEvent ev = ProcessDefinitionUtils.findBoundaryEvent(pd, eid, errorRef);
        if (ev == null) {
            ev = ProcessDefinitionUtils.findBoundaryEvent(pd, eid, null);
        }

        if (ev != null) {
            // на task висел "boundary error event" - обработка дальше пойдет по
            // его ветви
            log.debug("handleBpmError ['{}', '{}'] -> handle boundary error '{}'", bk, eid, errorRef);
            FlowUtils.followFlows(getEngine(), s, c, ev.getId());
        } else {
            // на task не было подходящего "boundary error event" - выполнение
            // текущего (под)процесса прекращается, ошибка передается
            // процессу-родителю
            log.debug("handleBpmError ['{}', '{}'] -> no boundary error", bk, eid, errorRef);
            BpmnErrorHelper.raiseError(c.getContext(), errorRef);
        }
    }
}
