package jet.scdp.bpm.engine.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import jet.scdp.bpm.api.ExecutionContext;

public class ExecutionContextVariableResolver extends ELResolver {

    private final ExecutionContext executionContext;

    public ExecutionContextVariableResolver(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return Object.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return Object.class;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        String k = (String) property;
        if (base == null && executionContext.hasVariable(k)) {
            context.setPropertyResolved(true);
            return executionContext.getVariable(k);
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
    }
}
