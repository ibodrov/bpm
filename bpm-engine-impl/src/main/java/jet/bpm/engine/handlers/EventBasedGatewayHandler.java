package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;

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
