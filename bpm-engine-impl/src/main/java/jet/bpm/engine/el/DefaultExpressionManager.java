package jet.bpm.engine.el;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.task.ServiceTaskRegistry;
import jet.bpm.engine.task.ServiceTaskResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExpressionManager implements ExpressionManager {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultExpressionManager.class);
    
    private final ExpressionFactory expressionFactory = new ExpressionFactoryImpl();
    private final ELResolver[] resolvers;
    
    public DefaultExpressionManager(ServiceTaskRegistry serviceTaskRegistry) {
        resolvers = new ELResolver[]{
            new ArrayELResolver(),
            new ListELResolver(),
            new MapELResolver(),
            new BeanELResolver(),
            new ServiceTaskResolver(serviceTaskRegistry)
        };
    }
    
    private ELResolver createResolver(ExecutionContext ctx) {
        CompositeELResolver cr = new CompositeELResolver();
        for (ELResolver r : resolvers) {
            cr.add(r);
        }
        cr.add(new ExecutionContextVariableResolver(ctx));
        return cr;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(ExecutionContext ctx, String expr, Class<T> type) {
        try {
            ELResolver r = createResolver(ctx);
            SimpleContext sc = new SimpleContext(r);
            sc.setVariable("execution", expressionFactory.createValueExpression(ctx, ExecutionContext.class));
            
            ValueExpression x = expressionFactory.createValueExpression(sc, expr, type);
            return (T) x.getValue(sc);
        } catch (ELException e) {
            log.error("eval ['{}', '{}'] -> error", expr, type, e);
            throw e;
        }
    }
    
}
