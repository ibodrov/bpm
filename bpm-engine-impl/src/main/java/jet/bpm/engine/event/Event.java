package jet.bpm.engine.event;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public final class Event implements Serializable {

    private final String name;
    private final String executionId;
    private final String groupId;
    private final String processBusinessKey;
    private final boolean exclusive;
    private final Date expiredAt;

    public Event(String name, String executionId, String groupId, String processBusinessKey, boolean exclusive, Date expiredAt) {
        this.name = name;
        this.executionId = executionId;
        this.groupId = groupId;
        this.processBusinessKey = processBusinessKey;
        this.exclusive = exclusive;
        this.expiredAt = expiredAt;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }

    public String getProcessBusinessKey() {
        return processBusinessKey;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.executionId);
        hash = 71 * hash + Objects.hashCode(this.groupId);
        hash = 71 * hash + Objects.hashCode(this.processBusinessKey);
        hash = 71 * hash + (this.exclusive ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.expiredAt);
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
        final Event other = (Event) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.executionId, other.executionId)) {
            return false;
        }
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (!Objects.equals(this.processBusinessKey, other.processBusinessKey)) {
            return false;
        }
        if (this.exclusive != other.exclusive) {
            return false;
        }
        if (!Objects.equals(this.expiredAt, other.expiredAt)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Event{" + "name=" + name + ", executionId=" + executionId + ", groupId=" + groupId + ", processBusinessKey=" + processBusinessKey + ", exclusive=" + exclusive + ", expiredAt=" + expiredAt + '}';
    }
}
