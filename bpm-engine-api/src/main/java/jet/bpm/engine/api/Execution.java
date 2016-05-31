package jet.bpm.engine.api;

import java.io.Serializable;
import java.util.UUID;

/**
 * Instance of the process execution.
 */
public interface Execution extends Serializable {

    /**
     * The ID of process instance. 
     */
    public UUID getId();

    /**
     * The business key of this process instance.
     */
    public String getBusinessKey();

    /**
     * Indicates when the execution is done (no more process steps to run).
     */
    public boolean isDone();
    
    /**
     * Indicates when the execution is suspended (waiting for an event).
     */
    public boolean isSuspended();
}
