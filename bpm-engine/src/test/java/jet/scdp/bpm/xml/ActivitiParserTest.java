package jet.scdp.bpm.xml;

import java.io.InputStream;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;
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
