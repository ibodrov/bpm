package jet.bpm.engine.xml.activiti;

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
        Parser p = new ActivitiParser();

        ProcessDefinition pd = p.parse(in);
        assertNotNull(pd);
        assertEquals("Process_1", pd.getId());
    }
}
