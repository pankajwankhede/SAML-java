import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class XmlUtils {
    public static String toString(XMLObject xmlObject) throws Exception {
        // Make sure DOM exists
        MarshallerFactory marshallerFactory = org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
        if (marshaller == null) {
            throw new RuntimeException("No marshaller for: " + xmlObject.getElementQName());
        }
        marshaller.marshall(xmlObject);

        // Convert DOM to string
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(xmlObject.getDOM()), new StreamResult(writer));

        return writer.toString();
    }
}
