package jet.scdp.bpm.xml;

import java.io.InputStream;
import jet.scdp.bpm.model.ProcessDefinition;

public interface Parser {

    ProcessDefinition parse(InputStream in) throws ParserException;
}
