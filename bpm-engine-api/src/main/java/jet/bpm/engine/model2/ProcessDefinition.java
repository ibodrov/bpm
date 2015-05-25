package jet.bpm.engine.model2;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ProcessDefinition implements Serializable {
    
    private final String id;
    private final String name;
    private final Map<String, ProcessElement> elements;

    private ProcessDefinition(ProcessDefinitionBuilder b) {
        this.id = b.id;
        this.name = b.name;
        this.elements = Collections.unmodifiableMap(b.elements);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, ProcessElement> getElements() {
        return elements;
    }
    
    public ProcessElement getElement(String id) {
        return elements.get(id);
    }
    
    public static final class ProcessDefinitionBuilder implements Serializable {
        
        private String id;
        private String name;
        private final Map<String, ProcessElement> elements = new HashMap<>();
        
        public ProcessDefinitionBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public ProcessDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public ProcessDefinitionBuilder element(ProcessElement e) {
            elements.put(e.getId(), e);
            return this;
        }
        
        public ProcessDefinition build() {
            return new ProcessDefinition(this);
        }
    }
}
