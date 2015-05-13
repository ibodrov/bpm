package jet.bpm.engine.xml;

import java.io.InputStream;
import jet.bpm.engine.model.graphics.ProcessGraphics;

public interface GraphicsParser {
    
    ProcessGraphics parse(InputStream in) throws ParserException;
}
