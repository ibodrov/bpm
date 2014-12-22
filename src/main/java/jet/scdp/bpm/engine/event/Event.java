package jet.scdp.bpm.engine.event;

import java.io.Serializable;
import java.util.Objects;

public final class Event implements Serializable {

    private final String id;
    private final String executionId;
    private final String groupId;
    private final boolean exclusive;
    private final String timeDate;
    private final String timeDuration;

    public Event(String id, String executionId, String groupId, boolean exclusive, String timeDate, String timeDuration) {
        this.id = id;
        this.executionId = executionId;
        this.groupId = groupId;
        this.exclusive = exclusive;
        this.timeDate = timeDate;
        this.timeDuration = timeDuration;
    }

    public String getId() {
        return id;
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

    public String getTimeDate() {
        return timeDate;
    }

    public String getTimeDuration() {
        return timeDuration;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.id);
        hash = 71 * hash + Objects.hashCode(this.executionId);
        hash = 71 * hash + Objects.hashCode(this.groupId);
        hash = 71 * hash + (this.exclusive ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.timeDate);
        hash = 71 * hash + Objects.hashCode(this.timeDuration);
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
        if (!Objects.equals(this.executionId, other.executionId)) {
            return false;
        }
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (this.exclusive != other.exclusive) {
            return false;
        }
        if (!Objects.equals(this.timeDate, other.timeDate)) {
            return false;
        }
        if (!Objects.equals(this.timeDuration, other.timeDuration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Event{" + "id=" + id + ", executionId=" + executionId + ", groupId=" + groupId + ", exclusive=" + exclusive + ", timeDate=" + timeDate + ", timeDuration=" + timeDuration + '}';
    }
}
