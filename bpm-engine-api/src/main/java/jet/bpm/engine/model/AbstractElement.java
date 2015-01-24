package jet.bpm.engine.model;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractElement implements Serializable {

    private final String id;

    public AbstractElement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractElement other = (AbstractElement) obj;
        return Objects.equals(this.id, other.id);
    }
}
