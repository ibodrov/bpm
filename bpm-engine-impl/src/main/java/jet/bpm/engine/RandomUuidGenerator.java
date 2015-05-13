package jet.bpm.engine;

import java.util.UUID;

public class RandomUuidGenerator implements UuidGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
