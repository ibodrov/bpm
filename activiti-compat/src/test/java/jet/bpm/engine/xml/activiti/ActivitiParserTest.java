package jet.bpm.engine.xml.activiti;

import jet.bpm.engine.xml.activiti.ActivitiParser;
import java.io.InputStream;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.xml.Parser;
import static org.junit.Assert.*;
import org.junit.Test;

public class ActivitiParserTest {

    @Test
    public void testComplex() throws Exception {
        InputStream in = ClassLoader.getSystemResourceAsStream("complex.bpmn");
        Parser p = new ActivitiParser();

        ProcessDefinition pd = p.parse(in);
        assertNotNull(pd);
        assertEquals("cpaChargingReserve", pd.getId());
    }
    
    @Test
    public void testMixed() throws Exception {
        InputStream in = ClassLoader.getSystemResourceAsStream("mixed.bpmn");
        Parser p = new ActivitiParser();

        ProcessDefinition pd = p.parse(in);
        assertNotNull(pd);
        
        SequenceFlow f = (SequenceFlow) pd.getChild("flow27");
        assertNotNull(f);
        assertEquals("${spaStatus == 'A'}", f.getExpression());
    }
}
