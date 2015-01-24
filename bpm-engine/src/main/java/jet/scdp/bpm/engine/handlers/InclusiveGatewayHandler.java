package jet.scdp.bpm.engine.handlers;

import java.util.Collection;
import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.api.ExecutionContext;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.FlowUtils;
import jet.scdp.bpm.engine.ProcessDefinitionUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.engine.commands.SuspendExecutionCommand;
import jet.scdp.bpm.model.InclusiveGateway;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;

/**
 * Обработчик элемента 'inclusive gateway'.
 */
public class InclusiveGatewayHandler extends AbstractElementHandler {

    public InclusiveGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition pd = getProcessDefinition(c);
        InclusiveGateway g = (InclusiveGateway) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        // если данный 'inclusive gateway' является открывающим, то после
        // прохода по всем ветвям, нужно приостановить выполнение
        if (g.getExit() != null) {
            s.push(new SuspendExecutionCommand(c.getContext()));
        }

        // продолжаем выполнение, при одном из двух условий:
        //
        // - если данный 'inclusive gateway' является открывающим и нам нужно
        //   просто обработать все его исходящие ветви;
        // - если данный 'incluse gateway' является завершающим и все его
        //   входящие ветви пройдены
        //
        if (g.getExit() != null || isActivationCompleted(pd, c)) {
            FlowUtils.followFlows(getEngine(), s, c);
        }
    }

    private boolean isActivationCompleted(ProcessDefinition pd, ProcessElementCommand c) throws ExecutionException {
        ExecutionContext ctx = c.getContext();

        Collection<SequenceFlow> flows = ProcessDefinitionUtils.findIncomingFlows(pd, c.getElementId());
        for (SequenceFlow flow : flows) {
            if (!ctx.isActivated(c.getProcessDefinitionId(), flow.getId())) {
                return false;
            }
        }

        return true;
    }
}
