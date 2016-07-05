package jet.bpm.engine.xml.camunda;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.ExclusiveGateway;
import jet.bpm.engine.model.InclusiveGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import jet.bpm.engine.xml.Parser;
import jet.bpm.engine.xml.ParserException;
import jet.bpm.engine.xml.camunda.model.AbstractXmlElement;
import jet.bpm.engine.xml.camunda.model.XmlDefinitions;
import jet.bpm.engine.xml.camunda.model.XmlEndEvent;
import jet.bpm.engine.xml.camunda.model.XmlEventBasedGateway;
import jet.bpm.engine.xml.camunda.model.XmlExclusiveGateway;
import jet.bpm.engine.xml.camunda.model.XmlInclusiveGateway;
import jet.bpm.engine.xml.camunda.model.XmlIntermediateCatchEvent;
import jet.bpm.engine.xml.camunda.model.XmlIntermediateThrowEvent;
import jet.bpm.engine.xml.camunda.model.XmlProcess;
import jet.bpm.engine.xml.camunda.model.XmlSequenceFlow;
import jet.bpm.engine.xml.camunda.model.XmlServiceTask;
import jet.bpm.engine.xml.camunda.model.XmlStartEvent;

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
        
        for (AbstractXmlElement e : p.getElements()) {
            if (e instanceof XmlStartEvent) {
                children.add(new StartEvent(e.getId()));
            } else if (e instanceof XmlEndEvent) {
                XmlEndEvent end = (XmlEndEvent) e;
                if (end.getErrorEventDefinition() != null) {
                    children.add(new EndEvent(e.getId(), end.getErrorEventDefinition().getErrorRef()));
                } else {
                    children.add(new EndEvent(e.getId()));
                }
            } else if (e instanceof XmlSequenceFlow) {
                // TODO expressions
                XmlSequenceFlow f = (XmlSequenceFlow) e;
                children.add(new SequenceFlow(e.getId(), f.getSourceRef(), f.getTargetRef()));
            } else if (e instanceof XmlServiceTask) {
                // TODO expressions
                children.add(new ServiceTask(e.getId()));
            } else if (e instanceof XmlEventBasedGateway) {
                children.add(new EventBasedGateway(e.getId()));
            } else if (e instanceof XmlExclusiveGateway) {
                // TODO default flow
                children.add(new ExclusiveGateway(e.getId()));
            } else if (e instanceof XmlInclusiveGateway) {
                children.add(new InclusiveGateway(e.getId()));
            } else if (e instanceof XmlIntermediateCatchEvent) {
                // TODO message refs & timers
                children.add(new IntermediateCatchEvent(e.getId()));
            } else if (e instanceof XmlIntermediateThrowEvent) {
                // TODO attachments, error refs and timeouts
                children.add(new BoundaryEvent(e.getId(), null, null));
            }
        }
        
        ProcessDefinition def = new ProcessDefinition(p.getId(), children);
        return def;
    }
}
