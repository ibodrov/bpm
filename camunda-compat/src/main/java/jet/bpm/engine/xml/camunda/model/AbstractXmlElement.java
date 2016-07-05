package jet.bpm.engine.xml.camunda.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlSeeAlso({
    XmlEndEvent.class,
    XmlEventBasedGateway.class,
    XmlExclusiveGateway.class,
    XmlInclusiveGateway.class,
    XmlIntermediateCatchEvent.class,
    XmlIntermediateThrowEvent.class,
    XmlSequenceFlow.class,
    XmlServiceTask.class,
    XmlStartEvent.class,
})
public class AbstractXmlElement implements Serializable {
    
    private String id;

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
