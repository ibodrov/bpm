package jet.bpm.engine.xml.camunda;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.xml.Parser;
import jet.bpm.engine.xml.ParserException;
import jet.bpm.engine.xml.camunda.model.XmlDefinitions;
import jet.bpm.engine.xml.camunda.model.XmlProcess;

public class CamundaParser implements Parser {
    
    private final JAXBContext ctx;

    public CamundaParser() {
        try {
            ctx = JAXBContext.newInstance("jet.bpm.engine.xml.camunda.model");
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProcessDefinition parse(InputStream in) throws ParserException {
        try {
            Unmarshaller m = ctx.createUnmarshaller();
            Object src = m.unmarshal(in);
            return convert((XmlDefinitions) src);
        } catch (JAXBException e) {
            throw new ParserException("JAXB error", e);
        }
    }
    
    private static ProcessDefinition convert(XmlDefinitions src) {
        XmlProcess p = src.getProcess();
        List<AbstractElement> children = new ArrayList<>();
        
        ProcessDefinition def = new ProcessDefinition(p.getId(), children);
        return def;
    }
}
