package jet.bpm.engine.el;

import jet.bpm.engine.api.ExecutionContext;

public interface ExpressionManager {

    <T> T eval(ExecutionContext ctx, String expr, Class<T> type);
}
