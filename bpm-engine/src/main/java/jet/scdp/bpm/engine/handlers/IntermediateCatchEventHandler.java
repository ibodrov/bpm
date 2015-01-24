package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.api.Engine;
import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.api.ExecutionContext;
import jet.scdp.bpm.engine.IdGenerator;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.engine.FlowUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.engine.el.ExpressionManager;
import jet.scdp.bpm.engine.event.Event;
import jet.scdp.bpm.model.IntermediateCatchEvent;
import jet.scdp.bpm.model.ProcessDefinition;

/**
 * Обработчик элемента 'intermediate catch event' - события процесса. При
 * обработке выполняется создание дочернего процесса, его приостановка и
 * привязка к событию. Созданный дочерний процесс может быть возобновлен с
 * помощью {@link Engine#resume(java.lang.String, java.lang.String, java.util.Map)}.
 */
public class IntermediateCatchEventHandler extends AbstractElementHandler {

    public IntermediateCatchEventHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        
        IdGenerator idg = getEngine().getIdGenerator();

        // создаем дочерний процесс, который начнет свое выполнение сразу с
        // первого элемента после обрабатываемого события
        DefaultExecution child = new DefaultExecution(idg.create(), s.getId(), s.getProcessBusinessKey());
        FlowUtils.followFlows(getEngine(), child, c, c.getElementId(), false);

        // приостановим и сохраним дочерный процесс. Он будет вызван кем-то
        // извне по факту наступления события
        child.setSuspended(true);
        getEngine().getPersistenceManager().save(child);
        
        ProcessDefinition pd = getProcessDefinition(c);
        IntermediateCatchEvent ice = (IntermediateCatchEvent)pd.getChild(c.getElementId());

        // сохраним привязку процесса к событию
        String evId = getEventId(ice);
        
        ExpressionManager em = getEngine().getExpressionManager();
        ExecutionContext ctx = c.getContext();
        String timeDate = eval(ice.getTimeDate(), ctx, em);
        String timeDuration = eval(ice.getTimeDuration(), ctx, em);
        
        Event e = new Event(evId, child.getId(), c.getGroupId(), c.isExclusive(), timeDate, timeDuration);
        getEngine().getEventManager().register(child.getProcessBusinessKey(), e);
    }

    /**
     * Возвращает ключ события. Если в событии указан тип BPMN message, то он
     * используется в качестве ключа. В противном случае, используется ID
     * элемента события.
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
