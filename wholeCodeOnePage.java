import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.util.XMLObjectSupport;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {
        InitializationService.initialize();

        // Load public X.509 cert from PEM
        String certPath = "src/main/resources/public-cert.pem";
        X509Certificate cert = loadCertificate(certPath);

        // Build SAML assertion
        Assertion assertion = buildAssertion("urn:auth0", "user@example.com");

        // Build SAML Response
        Response response = buildResponse("https://your-auth0-domain.auth0.com/samlp/YOUR_CLIENT_ID", assertion);

        // Marshall
        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(response);
        marshaller.marshall(response);

        // Convert to XML string
        String xml = xmlToString(response.getDOM());
        String encoded = Base64.getEncoder().encodeToString(xml.getBytes());

        // Print HTML form for POST
        System.out.println("<form method='POST' action='https://your-auth0-domain.auth0.com/samlp/YOUR_CLIENT_ID'>");
        System.out.println("<input type='hidden' name='SAMLResponse' value='" + encoded + "'/>");
        System.out.println("<input type='submit' value='Login via IdP'/>");
        System.out.println("</form>");
    }

    private static X509Certificate loadCertificate(String path) throws Exception {
        byte[] certBytes = Files.readAllBytes(Paths.get(path));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new java.io.ByteArrayInputStream(certBytes));
    }

    private static Assertion buildAssertion(String issuerStr, String userEmail) {
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setID("_" + UUID.randomUUID());
        assertion.setIssueInstant(Instant.now());

        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(issuerStr);
        assertion.setIssuer(issuer);

        NameID nameID = new NameIDBuilder().buildObject();
        nameID.setFormat(NameID.EMAIL);
        nameID.setValue(userEmail);

        Subject subject = new SubjectBuilder().buildObject();
        subject.setNameID(nameID);
        assertion.setSubject(subject);

        AttributeStatement attrStmt = new AttributeStatementBuilder().buildObject();
        Attribute attr = new AttributeBuilder().buildObject();
        attr.setName("email");

        XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .<XSStringBuilder>getBuilder(XSString.TYPE_NAME);
        XSString emailValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        emailValue.setValue(userEmail);

        attr.getAttributeValues().add(emailValue);
        attrStmt.getAttributes().add(attr);
        assertion.getAttributeStatements().add(attrStmt);

        return assertion;
    }

    private static Response buildResponse(String acsUrl, Assertion assertion) {
        Response response = new ResponseBuilder().buildObject();
        response.setID("_" + UUID.randomUUID());
        response.setIssueInstant(Instant.now());
        response.setDestination(acsUrl);
        response.getAssertions().add(assertion);

        Status status = new StatusBuilder().buildObject();
        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        statusCode.setValue(StatusCode.SUCCESS);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        return response;
    }

    private static String xmlToString(org.w3c.dom.Element element) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }
}
