package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionListener;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceFlowHandler extends AbstractElementHandler {

    private static final Logger log = LoggerFactory.getLogger(SequenceFlowHandler.class);

    public SequenceFlowHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);

        SequenceFlow flow = (SequenceFlow) ProcessDefinitionUtils.findElement(pd, c.getElementId());
        processListeners(s.getContext(), flow);

        // add to the stack the element processing command. Preserve group ID
        // and exclusiveness flag
        s.push(new ProcessElementCommand(c.getProcessDefinitionId(), flow.getTo(), c.getGroupId(), c.isExclusive()));
    }

    /**
     * Handle process flow listeners. Listener reference can be specified with
     * EL expression.
     * @param ctx current execution context.
     * @param f processing flow.
     * @throws ExecutionException
     */
    private void processListeners(ExecutionContext ctx, SequenceFlow f) throws ExecutionException {
        if (f.getListeners() == null) {
            return;
        }

        ExpressionManager em = getEngine().getExpressionManager();
        for (SequenceFlow.ExecutionListener l : f.getListeners()) {
            ExpressionType type = l.getType();
            String expr = l.getExpression();
            if (expr == null) {
                continue;
            }

            try {
                switch (type) {
                    case SIMPLE:
                        em.eval(ctx, expr, Object.class);
                        break;

                    case DELEGATE:
                        ExecutionListener d = em.eval(ctx, expr, ExecutionListener.class);
                        d.notify(ctx);
                }
            } catch (Exception e) {
                log.error("processListeners ['{}'] -> error", f.getId(), e);
                throw new ExecutionException("Unhandled listener exception", e);
            }
        }
    }
}
