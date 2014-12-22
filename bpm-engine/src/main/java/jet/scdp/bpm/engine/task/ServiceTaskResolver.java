package jet.scdp.bpm.engine.task;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

public class ServiceTaskResolver extends ELResolver {

    private final ServiceTaskRegistry registry;

    public ServiceTaskResolver(ServiceTaskRegistry registry) {
        this.registry = registry;
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
        if (base == null) {
            String key = (String) property;
            Object o = registry.getByKey(key);
            if (o != null) {
                context.setPropertyResolved(true);
            }
            return o;
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
