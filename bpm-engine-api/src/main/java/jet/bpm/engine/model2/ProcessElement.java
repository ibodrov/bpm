package jet.bpm.engine.model2;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ProcessElement implements Serializable {
    
    private final String id;
    private final String name;
    private final Map<String, Serializable> attributes;

    private ProcessElement(ProcessElementBuilder b) {
        this.id = b.id;
        this.name = b.name;
        this.attributes = Collections.unmodifiableMap(b.attributes);
    }

    public String getId() {
        return id;
    }

    public Map<String, Serializable> getAttributes() {
        return attributes;
    }
    
    public Serializable getAttribute(String k) {
        return attributes.get(k);
    }
    
    public static final class ProcessElementBuilder implements Serializable {
        
        private String id;
        private String name;
        private final Map<String, Serializable> attributes = new HashMap<>();
        
        public ProcessElementBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public ProcessElementBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public ProcessElementBuilder attribute(String k, Serializable v) {
            attributes.put(k, v);
            return this;
        }
        
        public ProcessElement build() {
            return new ProcessElement(this);
        }
    }
}
