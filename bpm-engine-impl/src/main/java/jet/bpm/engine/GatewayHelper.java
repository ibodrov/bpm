package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;

public final class GatewayHelper {

    private static final String FORK_COUNT_KEY = "__gw_fork_cnt";
    
    public static void inc(ExecutionContext ctx, int cnt) {
        Integer i = (Integer) ctx.getVariable(FORK_COUNT_KEY);
        if (i == null) {
            i = 0;
        }
        
        i += cnt;
        ctx.setVariable(FORK_COUNT_KEY, i);
    }
    
    public static void dec(ExecutionContext ctx) throws ExecutionException {
        Integer i = (Integer) ctx.getVariable(FORK_COUNT_KEY);
        if (i == null) {
            i = 0;
        }
        
        i--;
        if (i < 0) {
            i = 0;
        }
        
        ctx.setVariable(FORK_COUNT_KEY, i);
    }
    
    public static boolean isZero(ExecutionContext ctx) {
        Integer i = (Integer) ctx.getVariable(FORK_COUNT_KEY);
        return i != null ? i == 0 : true;
    }
    
    private GatewayHelper() {
    }
}
