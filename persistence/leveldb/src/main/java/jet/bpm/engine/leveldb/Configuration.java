package jet.bpm.engine.leveldb;

public class Configuration {

    private String eventPath;
    private String expiredEventIndexPath;
    private String businessKeyEventIndexPath;
    private String executionPath;

    private boolean syncWrite = true;

    public String getEventPath() {
        return eventPath;
    }

    public void setEventPath(String eventPath) {
        this.eventPath = eventPath;
    }

    public String getExpiredEventIndexPath() {
        return expiredEventIndexPath;
    }

    public void setExpiredEventIndexPath(String expiredEventIndexPath) {
        this.expiredEventIndexPath = expiredEventIndexPath;
    }

    public String getBusinessKeyEventIndexPath() {
        return businessKeyEventIndexPath;
    }

    public void setBusinessKeyEventIndexPath(String businessKeyEventIndexPath) {
        this.businessKeyEventIndexPath = businessKeyEventIndexPath;
    }

    public String getExecutionPath() {
        return executionPath;
    }

    public void setExecutionPath(String executionPath) {
        this.executionPath = executionPath;
    }

    public void setSyncWrite(boolean syncWrite) {
        this.syncWrite = syncWrite;
    }

    public boolean isSyncWrite() {
        return syncWrite;
    }
}
