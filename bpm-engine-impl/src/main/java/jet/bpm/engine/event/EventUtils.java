package jet.bpm.engine.event;

import java.util.Date;
import jet.bpm.engine.api.ExecutionException;
import org.joda.time.DateTime;
import org.joda.time.Period;

public final class EventUtils {

    public static Date resolveTimeDate(String duedate) throws ExecutionException {
        try {
            if (isDuration(duedate)) {
                return DateTime.now().plus(Period.parse(duedate)).toDate();
            }

            return DateTime.parse(duedate).toDate();
        } catch (Exception e) {
            throw new ExecutionException("couldn't resolve duedate: '" + duedate + "'", e);
        }
    }

    private static boolean isDuration(String time) {
        return time.startsWith("P");
    }

    private EventUtils() {
    }
}
