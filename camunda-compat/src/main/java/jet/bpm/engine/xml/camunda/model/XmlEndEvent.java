package jet.bpm.engine.xml.camunda.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = Constants.MODEL_NS, name = "endEvent")
public class XmlEndEvent extends AbstractXmlElement {
    
    private XmlErrorEventDefinition errorEventDefinition;

    public XmlErrorEventDefinition getErrorEventDefinition() {
        return errorEventDefinition;
    }

    public void setErrorEventDefinition(XmlErrorEventDefinition errorEventDefinition) {
        this.errorEventDefinition = errorEventDefinition;
    }
    
    @XmlRootElement(namespace = Constants.MODEL_NS, name = "errorEventDefinition")
    public static class XmlErrorEventDefinition implements Serializable {
        
        private String errorRef;

        @XmlAttribute
        public String getErrorRef() {
            return errorRef;
        }

        public void setErrorRef(String errorRef) {
            this.errorRef = errorRef;
        }
    }
}
