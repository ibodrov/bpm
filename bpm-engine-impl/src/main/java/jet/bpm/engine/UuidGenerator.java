package jet.bpm.engine;

import java.util.UUID;

public class UuidGenerator implements IdGenerator {

    @Override
    public String create() {
        return UUID.randomUUID().toString();
    }
}
