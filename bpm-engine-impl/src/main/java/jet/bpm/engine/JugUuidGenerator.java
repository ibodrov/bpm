package jet.bpm.engine;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import java.util.Random;
import java.util.UUID;

public class JugUuidGenerator implements UuidGenerator {

    private final NoArgGenerator delegate = Generators.randomBasedGenerator(new Random());
    
    @Override
    public UUID generate() {
        return delegate.generate();
    }
}
