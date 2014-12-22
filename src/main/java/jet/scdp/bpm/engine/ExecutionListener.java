package jet.scdp.bpm.engine;

public interface ExecutionListener {

    void notify(ExecutionContext ctx);
}
