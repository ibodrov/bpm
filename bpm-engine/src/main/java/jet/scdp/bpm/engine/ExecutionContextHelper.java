package jet.scdp.bpm.engine;

import jet.scdp.bpm.api.ExecutionContext;
import java.util.Set;
import jet.scdp.bpm.engine.el.ExpressionManager;
import jet.scdp.bpm.model.VariableMapping;

public final class ExecutionContextHelper {

    /**
     * Копирует переменные одного контекста в другой с помощью указанного набора
     * mappings. Если исходная переменная есть в mapping, но нет в исходном
     * контексте, то в целевом контексте переменная будет равна
     * <code>null</code>.
     * @param em ссылка на {@link ExpressionManager} для вычисления исходных
     * переменных
     * @param src исходный контекст
     * @param dst целевойй контекст
     * @param mappings правила копирования
     */
    public static void copyVariables(ExpressionManager em, ExecutionContext src, ExecutionContext dst, Set<VariableMapping> mappings) {
        if (mappings == null) {
            return;
        }
        
        for (VariableMapping m : mappings) {
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

    private ExecutionContextHelper() {
    }
}
