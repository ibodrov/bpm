package jet.bpm.engine.event;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public final class Event implements Serializable {

    private final UUID id;
    private final UUID executionId;
    private final UUID groupId;
    private final String name;
    private final String processBusinessKey;
    private final boolean exclusive;
    private final Date expiredAt;

    public Event(UUID id, UUID executionId, UUID groupId, String name, String processBusinessKey, boolean exclusive, Date expiredAt) {
        this.id = id;
        this.executionId = executionId;
        this.groupId = groupId;
        this.name = name;
        this.processBusinessKey = processBusinessKey;
        this.exclusive = exclusive;
        this.expiredAt = expiredAt;
    }

    public UUID getId() {
        return id;
    }
    
    public UUID getExecutionId() {
        return executionId;
    }

    public UUID getGroupId() {
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
        hash = 43 * hash + Objects.hashCode(this.id);
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + Objects.hashCode(this.executionId);
        hash = 43 * hash + Objects.hashCode(this.groupId);
        hash = 43 * hash + Objects.hashCode(this.processBusinessKey);
        hash = 43 * hash + (this.exclusive ? 1 : 0);
        hash = 43 * hash + Objects.hashCode(this.expiredAt);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
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
        return "Event{" + "id=" + id  + ", name=" + name + ", executionId=" + executionId + ", groupId=" + groupId + ", processBusinessKey=" + processBusinessKey + ", exclusive=" + exclusive + ", expiredAt=" + expiredAt + '}';
    }
}
