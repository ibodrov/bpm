package jet.bpm.engine.xml.camunda.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = Constants.MODEL_NS, name = "process")
public class XmlProcess implements Serializable {
    
    private String id;  
    private List<AbstractXmlElement> elements = new ArrayList<>();

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @XmlElementRef
    public List<AbstractXmlElement> getElements() {
        return elements;
    }

    public void setElements(List<AbstractXmlElement> elements) {
        this.elements = elements;
    }
}
