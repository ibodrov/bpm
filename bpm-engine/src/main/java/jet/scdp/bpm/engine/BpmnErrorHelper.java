package jet.scdp.bpm.engine;

import jet.scdp.bpm.api.ExecutionContext;

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
