package jet.bpm.engine.api;

import java.io.Serializable;

public interface Execution extends Serializable {

    public String getId();

    public String getParentId();

    public String getProcessBusinessKey();

    public boolean isDone();
    
    public boolean isSuspended();
}
