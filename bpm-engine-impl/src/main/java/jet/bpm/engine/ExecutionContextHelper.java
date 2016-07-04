package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import java.util.Set;
import jet.bpm.engine.api.Execution;
import jet.bpm.engine.el.ExpressionManager;
import jet.bpm.engine.model.VariableMapping;

public final class ExecutionContextHelper {
    
    public static final String PROCESS_BUSINESS_KEY = "__processBusinessKey";

    /**
     * Copy variables from one context to another. If the source variable is
     * present in mapping, but not in source context, then target variable will
     * be <code>null</code>.
     * @param em reference to the {@link ExpressionManager}. Used to compute
     * variable values.
     * @param src source context.
     * @param dst target context.
     * @param mapping variables mapping.
     */
    public static void copyVariables(ExpressionManager em, ExecutionContext src, ExecutionContext dst, Set<VariableMapping> mapping) {
        if (mapping == null) {
            return;
        }
        
        for (VariableMapping m : mapping) {
            String source = m.getSource();
            String sourceExpression = m.getSourceExpression();
            
            Object v = null;
            if (source != null) {
                v = src.getVariable(source);
            } else if (sourceExpression != null) {
                v = em.eval(src, sourceExpression, Object.class);
            }
            
            dst.setVariable(m.getTarget(), v);
        }
    }
    
    public static void copyVariables(ExecutionContext src, ExecutionContext dst) {
        Set<String> keys = src.getVariableNames();
        for (String k : keys) {
            Object v = src.getVariable(k);
            dst.setVariable(k, v);
        }
    }
    
    public static void fillBasicVariables(Execution execution, ExecutionContext ctx) {
        ctx.setVariable(PROCESS_BUSINESS_KEY, execution.getBusinessKey());
    }

    private ExecutionContextHelper() {
    }
}
