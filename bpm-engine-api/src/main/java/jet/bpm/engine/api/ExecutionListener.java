package jet.bpm.engine.api;

/**
 * Execution listener. Can be declared for sequence flows in the process
 * definition. Fires on the flow activation, before the execution moves to the
 * next element of the process.
 */
public interface ExecutionListener {

    /**
     * Fires on the flow activation.
     * @param ctx the execution context of the current process instance.
     */
    void notify(ExecutionContext ctx);
}
