package jet.bpm.engine.event;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.api.ExecutionException;

public class DirectEventDispatcher implements EventDispatcher {

    private final AbstractEngine engine;

    public DirectEventDispatcher(AbstractEngine engine) {
        this.engine = engine;
    }

    @Override
    public void dispatch(Event e) throws ExecutionException {
        engine.resume(e, null);
    }
}
