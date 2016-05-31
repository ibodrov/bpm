package jet.bpm.engine.api;

public interface ExecutionListener {

    /**
     * Fires on the flow activation.
     * @param ctx the execution context of the current process instance.
     */
    void notify(ExecutionContext ctx);
}
