package jet.bpm.engine.handlers;

import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.IdGenerator;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;

/**
 * Itermediate event handler. Its job is to create child execution, suspend
 * it and link it with the event.
 */
public class IntermediateCatchEventHandler extends AbstractElementHandler {

    public IntermediateCatchEventHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        
        IdGenerator idg = getEngine().getIdGenerator();

        // create child execution, which will start right from the first element
        // after current
        DefaultExecution child = new DefaultExecution(idg.create(), s.getId(), s.getBusinessKey());
        FlowUtils.followFlows(getEngine(), child, c, c.getElementId(), false);

        // suspend and save child execution. Its will be resumed by someone
        // outside of current process
        child.setSuspended(true);
        getEngine().getPersistenceManager().save(child);
        
        ProcessDefinition pd = getProcessDefinition(c);
        IntermediateCatchEvent ice = (IntermediateCatchEvent)pd.getChild(c.getElementId());

        // link execution with the event
        String evId = getEventId(ice);
        
        ExpressionManager em = getEngine().getExpressionManager();
        ExecutionContext ctx = c.getContext();
        String timeDate = eval(ice.getTimeDate(), ctx, em);
        String timeDuration = eval(ice.getTimeDuration(), ctx, em);
        
        Event e = new Event(evId, child.getId(), c.getGroupId(), c.isExclusive(), timeDate, timeDuration);
        getEngine().getEventManager().register(child.getBusinessKey(), e);
    }

    /**
     * Return the event ID. If the specifed event has message reference, it will
     * be used as an event ID. Otherwise, element ID will be used.
     */
    private String getEventId(IntermediateCatchEvent ev) {
        return ev.getMessageRef() != null ? ev.getMessageRef() : ev.getId();
    }
    
    private String eval(String expr, ExecutionContext ctx, ExpressionManager em) {
        if (expr == null || expr.trim().isEmpty()) {
            return null;
        }
        return em.eval(ctx, expr, String.class);
    }
}
