package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;

public final class BpmnErrorHelper {

    private static final String KEY = "__bpmn_raised_error";
    
    public static String getRaisedError(ExecutionContext ctx) {
        return (String)ctx.getVariable(KEY);
    }
    
    /**
     * Raise the error to parent execution.
     * @param ctx the current execution context.
     * @param errorRef error reference.
     */
    public static void raiseError(ExecutionContext ctx, String errorRef) {
        ctx.setVariable(KEY, errorRef);
    }
    
    /**
     * Clears raised error in the given context.
     * @param ctx the execution context.
     */
    public static void clear(ExecutionContext ctx) {
        ctx.removeVariable(KEY);
    }

    private BpmnErrorHelper() {
    }
}
