package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.commands.ProcessElementCommand;

/**
 * Обработчик элемента 'start event'. Просто осуществляет переход к
 * следующему элементу.
 */
public class StartEventHandler extends AbstractElementHandler {

    public StartEventHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        FlowUtils.followFlows(getEngine(), s, c, null, false);
    }
}
