package jet.bpm.engine.xml.activiti;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import jet.bpm.engine.model.diagram.Label;
import jet.bpm.engine.model.diagram.Bounds;
import jet.bpm.engine.model.diagram.Edge;
import jet.bpm.engine.model.diagram.ProcessDiagram;
import jet.bpm.engine.model.diagram.Shape;
import jet.bpm.engine.model.diagram.Waypoint;
import jet.bpm.engine.xml.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import jet.bpm.engine.xml.DiagramParser;

public class ActivitiDiagramParser implements DiagramParser {

    private static final Logger log = LoggerFactory.getLogger(ActivitiDiagramParser.class);

    @Override
    public ProcessDiagram parse(InputStream in) throws ParserException {
        if (in == null) {
            throw new NullPointerException("Input cannot be null");
        }

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser p = spf.newSAXParser();

            Handler h = new Handler();
            p.parse(in, h);

            return h.graphics;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ParserException("Parsing error", e);
        }
    }

    private static final class Handler extends DefaultHandler {

        private StringBuilder text;

        private String id;

        private String elementId;

        private Waypoint waypoint;

        private Label label;

        private Bounds bounds;

        private List<Waypoint> waypoints;

        private ProcessDiagram graphics;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            log.debug("startElement ['{}']", qName);

            switch (qName) {
                case "process":
                    String processId = attributes.getValue("id");
                    graphics = new ProcessDiagram(processId);
                    break;                
                case "bpmndi:BPMNShape": {
                    id = attributes.getValue("id");
                    elementId = attributes.getValue("bpmnElement");

                    break;
                }
                case "omgdc:Bounds": {
                    double x = Double.valueOf(attributes.getValue("x"));
                    double y = Double.valueOf(attributes.getValue("y"));
                    double w = Double.valueOf(attributes.getValue("width"));
                    double h = Double.valueOf(attributes.getValue("height"));

                    bounds = new Bounds(x, y, w, h);

                    break;
                }
                case "bpmndi:BPMNEdge": {
                    id = attributes.getValue("id");
                    elementId = attributes.getValue("bpmnElement");

                    waypoints = new ArrayList<>();

                    break;
                }
                case "omgdi:waypoint": {
                    double x = Double.valueOf(attributes.getValue("x"));
                    double y = Double.valueOf(attributes.getValue("y"));

                    waypoint = new Waypoint(x, y);

                    break;
                }
                case "bpmndi:BPMNLabel":

                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (text != null) {
                text.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            log.debug("endElement ['{}']", qName);

            switch (qName) {
                case "bpmndi:BPMNShape": {
                    Shape shape = new Shape(id, elementId, bounds);
                    graphics.getShapes().add(shape);

                    bounds = null;

                    break;
                }
                case "omgdc:Bounds": {

                    break;
                }
                case "bpmndi:BPMNEdge": {
                    Edge edge = new Edge(id, elementId, label, waypoints);
                    graphics.getEdges().add(edge);

                    label = null;
                    waypoints = null;

                    break;
                }
                case "omgdi:waypoint": {
                    waypoints.add(waypoint);

                    break;
                }
                case "bpmndi:BPMNLabel":
                    label = new Label(bounds);

                    bounds = null;

                    break;
            }
        }
    }
}
