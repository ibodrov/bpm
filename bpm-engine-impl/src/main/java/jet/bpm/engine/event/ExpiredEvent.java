package jet.bpm.engine.event;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ExpiredEvent implements Serializable {

    private final UUID id;
    private final Date expiredAt;

    public ExpiredEvent(UUID id, Date expiredAt) {
        this.id = id;
        this.expiredAt = expiredAt;
    }

    public UUID geId() {
        return id;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }
}
