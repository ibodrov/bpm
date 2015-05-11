package jet.bpm.engine.event;

import java.io.Serializable;
import java.util.Date;

public class ExpiredEvent implements Serializable {

    private final String processBusinessKey;
    private final String eventName;
    private final Date expiredAt;

    public ExpiredEvent(String processBusinessKey, String eventName, Date expiredAt) {
        this.processBusinessKey = processBusinessKey;
        this.eventName = eventName;
        this.expiredAt = expiredAt;
    }

    public String getProcessBusinessKey() {
        return processBusinessKey;
    }

    public String getEventName() {
        return eventName;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }
}
