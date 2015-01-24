package jet.bpm.engine.handlers;

import java.util.Collection;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;
import jet.bpm.engine.model.InclusiveGateway;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;

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
