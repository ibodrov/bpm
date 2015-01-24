package jet.bpm.engine.xml;

import java.io.InputStream;
import jet.bpm.engine.model.ProcessDefinition;

public interface Parser {

    ProcessDefinition parse(InputStream in) throws ParserException;
}
