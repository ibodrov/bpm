package jet.bpm.engine.xml;

import java.io.InputStream;
import jet.bpm.engine.model.diagram.ProcessDiagram;

public interface DiagramParser {
    
    ProcessDiagram parse(InputStream in) throws ParserException;
}
