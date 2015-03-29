package jet.bpm.engine.api;

import java.io.Serializable;

/**
 * Process execution instance.
 */
public interface Execution extends Serializable {

    /**
     * The ID of process instance. 
     */
    public String getId();

    /**
     * The ID of parent process instance.
     */
    public String getParentId();

    /**
     * The business key of this process instance.
     */
    public String getBusinessKey();

    /**
     * Indicates when execution is done (no more process steps to run).
     */
    public boolean isDone();
    
    /**
     * Indicates when execution is suspended (waiting for an event).
     */
    public boolean isSuspended();
}
