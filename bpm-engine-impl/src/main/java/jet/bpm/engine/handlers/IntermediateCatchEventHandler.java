package jet.bpm.engine.handlers;

import java.util.Date;
import java.util.UUID;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.EventMapHelper;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.PersistExecutionCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Itermediate event handler. Its job is to create child execution, suspend it
 * and link it with the event.
 */
public class IntermediateCatchEventHandler extends AbstractElementHandler {
    
    public static Date parseIso8601(String s) {
        return DateTime.parse(s).toDate();
    }

    public IntermediateCatchEventHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        
        Event e = makeEvent(c, s);
        
        ProcessDefinition pd = getProcessDefinition(c);

        if (c.getGroupId() != null) {
            // grouped event
            SequenceFlow next = ProcessDefinitionUtils.findOutgoingFlow(pd, c.getElementId());
            
            EventMapHelper.put(s, e,
                    new PersistExecutionCommand(),
                    new ProcessElementCommand(pd.getId(), next.getId(), c.getGroupId(), c.isExclusive()));
        } else {
            // standalone event
            s.push(new SuspendExecutionCommand());
            
            SequenceFlow next = ProcessDefinitionUtils.findOutgoingFlow(pd, c.getElementId());
            s.push(new ProcessElementCommand(pd.getId(), next.getId(), c.getGroupId(), c.isExclusive()));
        }
        
        getEngine().getEventManager().add(e);
    }

    private Event makeEvent(ProcessElementCommand c, DefaultExecution s) throws ExecutionException {
        ProcessDefinition pd = getProcessDefinition(c);
        IntermediateCatchEvent ice = (IntermediateCatchEvent) ProcessDefinitionUtils.findElement(pd, c.getElementId());
        
        UUID id = getEngine().getUuidGenerator().generate();
        String name = getEventName(ice);
        
        ExpressionManager em = getEngine().getExpressionManager();
        ExecutionContext ctx = s.getContext();
        Date timeDate = parseTimeDate(ice.getTimeDate(), c, ctx, em);
        String timeDuration = eval(ice.getTimeDuration(), ctx, em, String.class);
        Date expiredAt = timeDate != null ? timeDate : parseDuration(timeDuration);
        
        Event e = new Event(id, s.getId(), c.getGroupId(), name, s.getBusinessKey(), c.isExclusive(), expiredAt);
        return e;
    }
    
    private Date parseTimeDate(String s, ProcessElementCommand c, ExecutionContext ctx, ExpressionManager em) throws ExecutionException {
        Object v = eval(s, ctx, em, Object.class);
        if (v == null) {
            return null;
        }
        
        if (v instanceof String) {
            return parseIso8601(s);
        } else if (v instanceof Date) {
            return (Date) v;
        } else {
            throw new ExecutionException("Invalid timeDate format in process '%s' in element '%s': '%s'", c.getProcessDefinitionId(), c.getElementId(), s);
        }
    }

    public static Date parseDuration(String s) throws ExecutionException {
        if(s == null) {
            return null;
        }

        if (isDuration(s)) {
            return DateTime.now().plus(Period.parse(s)).toDate();
        } else {
            throw new ExecutionException("Invalid duration format: '%s'", s);
        }
    }

    private static boolean isDuration(String time) {
        return time.startsWith("P");
    }
    
    private static String getEventName(IntermediateCatchEvent e) {
        return e.getMessageRef() != null ? e.getMessageRef() : e.getId();
    }

    private <T> T eval(String expr, ExecutionContext ctx, ExpressionManager em, Class<T> type) {
        if (expr == null || expr.trim().isEmpty()) {
            return null;
        }
        return em.eval(ctx, expr, type);
    }
}
