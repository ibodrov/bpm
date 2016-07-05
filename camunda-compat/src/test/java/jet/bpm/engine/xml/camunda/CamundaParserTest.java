package jet.bpm.engine.xml.camunda;

import java.io.InputStream;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.xml.Parser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class CamundaParserTest {
    
    @Test
    public void testSimple() throws Exception {
        InputStream in = ClassLoader.getSystemResourceAsStream("camunda.bpmn");
        Parser p = new CamundaParser();

        ProcessDefinition pd = p.parse(in);
        assertNotNull(pd);
        assertEquals("Process_1", pd.getId());
    }
    
    @Test
    public void testComplex() throws Exception {
        InputStream in = ClassLoader.getSystemResourceAsStream("complex.bpmn");
        Parser p = new CamundaParser();

        ProcessDefinition pd = p.parse(in);
        assertNotNull(pd);
        assertNotNull(pd.getChildren());
        assertEquals(21, pd.getChildren().size());
    }
}
