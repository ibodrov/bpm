package jet.scdp.bpm.model;

public class IntermediateCatchEvent extends AbstractElement {

    private final String messageRef;
    private final String timeDate;
    private final String timeDuration;

    public IntermediateCatchEvent(String id) {
        this(id, null, null, null);
    }
    
    public IntermediateCatchEvent(String id, String messageRef) {
        this(id, messageRef, null, null);
    }

    public IntermediateCatchEvent(String id, String messageRef, String timeDate, String timeDuration) {
        super(id);
        this.messageRef = messageRef;
        this.timeDate = timeDate;
        this.timeDuration = timeDuration;
    }
    
    public String getMessageRef() {
        return messageRef;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public String getTimeDuration() {
        return timeDuration;
    }
}
