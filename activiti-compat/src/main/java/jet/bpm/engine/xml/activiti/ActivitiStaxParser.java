package jet.bpm.engine.xml.activiti;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import jet.bpm.engine.model2.ProcessDefinition;
import jet.bpm.engine.model2.ProcessDefinition.ProcessDefinitionBuilder;
import jet.bpm.engine.model2.ProcessElement;
import jet.bpm.engine.model2.ProcessElement.ProcessElementBuilder;
import jet.bpm.engine.xml.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivitiStaxParser {

    private static final Logger log = LoggerFactory.getLogger(ActivitiStaxParser.class);

    private static final String ID_ATTR = "id";
    private static final String NAME_ATTR = "name";
    
    private static final String PROCESS_TAG = "process";
    private static final String START_EVENT_TAG = "startEvent";

    public ProcessDefinition parse(InputStream in) throws ParserException {
        try {
            XMLInputFactory f = XMLInputFactory.newInstance();
            XMLEventReader r = f.createXMLEventReader(in);

            try {
                while (r.hasNext()) {
                    XMLEvent e = r.nextEvent();
                    if (e.isStartElement()) {
                        StartElement start = e.asStartElement();
                        if (getTagName(start).equals(PROCESS_TAG)) {
                            return parseProcess(r, start);
                        }
                    }
                }

                throw new ParserException("Invalid XML format: 'process' tag not found");
            } finally {
                r.close();
            }
        } catch (XMLStreamException e) {
            throw new ParserException("Parsing error", e);
        }
    }

    private static String getTagName(StartElement e) {
        return e.getName().getLocalPart();
    }

    private static String getAttributeValue(StartElement e, String k) throws ParserException {
        Attribute a = e.getAttributeByName(new QName(k));
        if (a == null) {
            throw new ParserException("Invalid XML format: missing attribute '" + k + "' in tag '" + e.getName().getLocalPart() + "'");
        }
        return a.getValue();
    }

    private static ProcessDefinition parseProcess(XMLEventReader r, StartElement start) throws XMLStreamException, ParserException {
        String id = getAttributeValue(start, ID_ATTR);

        ProcessDefinitionBuilder b = new ProcessDefinitionBuilder()
                .id(id)
                .name(getAttributeValue(start, NAME_ATTR));

        while (r.hasNext()) {
            XMLEvent e = r.nextEvent();
            if (e.isStartElement()) {
                StartElement s = e.asStartElement();
                String tag = getTagName(s);
                switch (tag) {
                    case START_EVENT_TAG:
                        b.element(parseStartEvent(start));
                        break;
                    default:
                        log.warn("parseProcess ['{}'] -> unknown tag '{}'", id, tag);
                }
            } else if (e.isEndElement()) {
                EndElement end = e.asEndElement();
                if (end.getName().getLocalPart().equals(PROCESS_TAG)) {
                    break;
                }
            }
        }

        return b.build();
    }
    
    private static ProcessElement parseStartEvent(StartElement start) throws ParserException {
        return new ProcessElementBuilder()
                .id(getAttributeValue(start, ID_ATTR))
                .name(getAttributeValue(start, NAME_ATTR))
                .build();
    }
}
