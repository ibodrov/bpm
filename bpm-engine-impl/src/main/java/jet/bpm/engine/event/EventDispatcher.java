package jet.bpm.engine.event;

import jet.bpm.engine.api.ExecutionException;

public interface EventDispatcher {

    void dispatch(Event e) throws ExecutionException;
}
