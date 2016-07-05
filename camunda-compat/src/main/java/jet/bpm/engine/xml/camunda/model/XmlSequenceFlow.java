package jet.bpm.engine.xml.camunda.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = Constants.MODEL_NS, name = "sequenceFlow")
public class XmlSequenceFlow extends AbstractXmlElement {
    
    private String sourceRef;
    private String targetRef;

    @XmlAttribute
    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    @XmlAttribute
    public String getTargetRef() {
        return targetRef;
    }

    public void setTargetRef(String targetRef) {
        this.targetRef = targetRef;
    }
}
