package jet.bpm.engine.handlers;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ProcessElementCommand;

public class InclusiveGatewayHandler extends ParallelGatewayHandler {

    public InclusiveGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        super.handle(s, c);
    }
}
