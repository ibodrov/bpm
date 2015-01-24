package jet.scdp.bpm.engine.el;

import jet.scdp.bpm.api.ExecutionContext;

public interface ExpressionManager {

    <T> T eval(ExecutionContext ctx, String expr, Class<T> type);
}
