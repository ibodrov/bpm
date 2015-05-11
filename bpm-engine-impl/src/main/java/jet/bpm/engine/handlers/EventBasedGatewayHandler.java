package jet.bpm.engine.handlers;

import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;

public class EventBasedGatewayHandler extends AbstractElementHandler {

    public EventBasedGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        // add to the stack process suspension command. It is expected that it
        // will be called when all outgoind sequence flows of this gateway
        // is done.
        s.push(new SuspendExecutionCommand());

        // add to the stack all the element of outgoing flows of this gateway
        // and mark them with 'exclusiveness' flag (because in the event gateway
        // only one flow can complete)
        FlowUtils.followFlows(getEngine(), s, c, c.getElementId(), true);
    }
}
