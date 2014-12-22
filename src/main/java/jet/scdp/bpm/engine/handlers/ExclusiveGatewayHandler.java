package jet.scdp.bpm.engine.handlers;

import java.util.Iterator;
import java.util.List;
import jet.scdp.bpm.engine.Execution;
import jet.scdp.bpm.engine.ExecutionContext;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.ExecutionException;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.engine.el.ExpressionManager;
import jet.scdp.bpm.model.ExclusiveGateway;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Обработчик элемента 'exclusive gateway'.
 */
public class ExclusiveGatewayHandler extends AbstractElementHandler {

    private static final Logger log = LoggerFactory.getLogger(ExclusiveGatewayHandler.class);

    public ExclusiveGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(Execution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        String nextId = null;

        ProcessDefinition pd = getProcessDefinition(c);

        // найдем все исходящие flow. Если на них были какие-то EL-выражения,
        // то вычислим их
        List<SequenceFlow> flows = ProcessDefinitionUtils.findOutgoingFlows(pd, c.getElementId());
        for (Iterator<SequenceFlow> i = flows.iterator(); i.hasNext();) {
            // вычислим значение
            SequenceFlow f = i.next();
            if (f.getExpression() != null) {
                i.remove();
                if (eval(c.getContext(), f)) {
                    // нашелся flow с выражением, которое вычислилось в true
                    nextId = f.getId();
                    break;
                }
            }
        }

        ExclusiveGateway element = (ExclusiveGateway) pd.getChild(c.getElementId());

        if (nextId == null && !flows.isEmpty()) {
            // остались только те flow, на которых не было EL-выражений
            String defaultFlow = element.getDefaultFlow();
            if (defaultFlow != null) {
                // задан flow по умолчанию, попробуем его
                for (SequenceFlow f : flows) {
                    if (f.getId().equals(defaultFlow)) {
                        nextId = f.getId();
                        break;
                    }
                }
            } else {
                // default flow не задан, возьмем первый из оставшихся
                nextId = flows.iterator().next().getId();
            }
        }

        if (nextId == null) {
            // ничего не найдено или ни один flow с EL-выражением не был
            // вычислен в true
            throw new ExecutionException("No valid outgoing flows for " + c.getElementId() + " and no default flows");
        }

        log.debug("'{}' was selected", nextId);
        s.push(new ProcessElementCommand(pd.getId(), nextId, c.getContext()));
    }

    private boolean eval(ExecutionContext ctx, SequenceFlow f) {
        String expr = f.getExpression();

        ExpressionManager em = getEngine().getExpressionManager();
        boolean b = em.eval(ctx, expr, Boolean.class);

        log.debug("eval ['{}', '{}'] -> {}", f.getId(), f.getExpression(), b);
        return b;
    }
}
