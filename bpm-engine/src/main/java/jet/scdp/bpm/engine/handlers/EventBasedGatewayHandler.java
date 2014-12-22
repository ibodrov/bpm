package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.engine.FlowUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;
import jet.scdp.bpm.engine.commands.SuspendExecutionCommand;

/**
 * Обработчик элемента 'event-based gateway'.
 */
public class EventBasedGatewayHandler extends AbstractElementHandler {

    public EventBasedGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        // помещаем на стек команду приостановки процесса. Ожидается, что она
        // будет вызвана, когда завершится обработка всех 'sequence flow'
        // исходящих из данного 'gateway'
        s.push(new SuspendExecutionCommand(c.getContext()));

        // помещаем на стек все элементы исходящих 'flow'. Т.е. exclusive
        // gateway подразумевает, что только одна из ветвей будет выполнена,
        // то передадим признак эклюзивности
        FlowUtils.followFlows(getEngine(), s, c, c.getElementId(), true);
    }
}
