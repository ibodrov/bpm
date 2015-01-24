package jet.bpm.engine.model;

import java.io.Serializable;
import java.util.Objects;

public class VariableMapping implements Serializable {
    
    private final String source;
    private final String sourceExpression;
    private final String target;

    public VariableMapping(String source, String sourceExpression, String target) {
        this.source = source;
        this.sourceExpression = sourceExpression;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getSourceExpression() {
        return sourceExpression;
    }
    
    public String getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.source);
        hash = 83 * hash + Objects.hashCode(this.sourceExpression);
        hash = 83 * hash + Objects.hashCode(this.target);
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
        final VariableMapping other = (VariableMapping) obj;
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.sourceExpression, other.sourceExpression)) {
            return false;
        }
        return Objects.equals(this.target, other.target);
    }
}
