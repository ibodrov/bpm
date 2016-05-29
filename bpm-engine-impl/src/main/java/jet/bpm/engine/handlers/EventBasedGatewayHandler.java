package jet.bpm.engine.handlers;

import java.util.UUID;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.FlowUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.ProcessEventMappingCommand;

public class EventBasedGatewayHandler extends AbstractElementHandler {

    public EventBasedGatewayHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        // add the process suspension command to the stack. It is expected that
        // it will be called when all outgoing sequence flows of this gateway
        // is done.
        s.push(new ProcessEventMappingCommand());

        UUID groupId = getEngine().getUuidGenerator().generate();

        // add all elements of outgoing flows of this gateway to the stack 
        // and mark them with the 'exclusiveness' flag (this way only one flow
        // can be completed)
        FlowUtils.followFlows(getEngine(), s, c, groupId, true);
    }
}
