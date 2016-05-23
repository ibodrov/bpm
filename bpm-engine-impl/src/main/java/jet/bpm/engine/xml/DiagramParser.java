package jet.bpm.engine.xml;

import java.io.InputStream;
import jet.bpm.engine.model.diagram.ProcessGraphics;

public interface DiagramParser {
    
    ProcessGraphics parse(InputStream in) throws ParserException;
}
