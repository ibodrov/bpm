package jet.bpm.engine.api;

public interface ExecutionListener {

    void notify(ExecutionContext ctx);
}
