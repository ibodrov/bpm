package jet.bpm.engine;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class JugUuidGenerator implements UuidGenerator {

    private final NoArgGenerator delegate = Generators.randomBasedGenerator(ThreadLocalRandom.current());
    
    @Override
    public UUID generate() {
        return delegate.generate();
    }
}
