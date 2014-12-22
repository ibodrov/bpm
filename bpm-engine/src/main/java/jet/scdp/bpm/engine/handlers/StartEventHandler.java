package jet.scdp.bpm.engine.handlers;

import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.engine.FlowUtils;
import jet.scdp.bpm.engine.commands.ProcessElementCommand;

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
