package jet.bpm.engine.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessDefinition extends AbstractElement {

    private final Map<String, AbstractElement> entities;

    public ProcessDefinition(String id, Collection<AbstractElement> children) {
        super(id);

        Map<String, AbstractElement> m = new LinkedHashMap<>();
        if (children != null) {
            for (AbstractElement c : children) {
                m.put(c.getId(), c);
            }
        }

        this.entities = Collections.unmodifiableMap(m);
    }

    public AbstractElement getChild(String id) {
        return entities.get(id);
    }

    public boolean hasChild(String id) {
        return entities.containsKey(id);
    }

    public Collection<AbstractElement> getChildren() {
        return entities.values();
    }
}
