package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;

public final class BpmnErrorHelper {

    private static final String KEY = "__bpmn_raised_error";
    
    public static String getRaisedError(ExecutionContext ctx) {
        return (String)ctx.getVariable(KEY);
    }
    
    public static void raiseError(ExecutionContext ctx, String errorRef) {
        ctx.setVariable(KEY, errorRef);
    }
    
    public static void clear(ExecutionContext ctx) {
        ctx.removeVariable(KEY);
    }

    private BpmnErrorHelper() {
    }
}
