import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.xmlsec.signature.*;
import org.opensaml.xmlsec.signature.impl.*;
import org.opensaml.xmlsec.signature.support.SAMLSignatureProfileValidator;

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
        // Init OpenSAML
        InitializationService.initialize();

        // Load X.509 PEM certificate
        X509Certificate cert = loadCertificate("src/main/resources/public-cert.pem");

        // Create Assertion
        Assertion assertion = buildAssertion("urn:auth0", "user@example.com");

        // Optionally embed X.509 cert inside KeyInfo (only if needed for metadata / debugging)
        KeyInfo keyInfo = buildKeyInfo(cert);

        // Create SAML Response
        Response response = buildResponse("https://your-auth0-domain.auth0.com/samlp/YOUR_CLIENT_ID", assertion);

        // Marshall to DOM
        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(response);
        marshaller.marshall(response);

        // Convert to base64
        String xml = toString(response.getDOM());
        String base64 = Base64.getEncoder().encodeToString(xml.getBytes());

        // Print HTML form
        System.out.println("<form method='POST' action='https://your-auth0-domain.auth0.com/samlp/YOUR_CLIENT_ID'>");
        System.out.println("<input type='hidden' name='SAMLResponse' value='" + base64 + "'/>");
        System.out.println("<input type='submit' value='Submit'/>");
        System.out.println("</form>");
    }

    static X509Certificate loadCertificate(String pemPath) throws Exception {
        byte[] certBytes = Files.readAllBytes(Paths.get(pemPath));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new java.io.ByteArrayInputStream(certBytes));
    }

    static Assertion buildAssertion(String issuerStr, String userEmail) {
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

        return assertion;
    }

    static Response buildResponse(String acsUrl, Assertion assertion) {
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

    static KeyInfo buildKeyInfo(X509Certificate cert) throws Exception {
        X509Data x509Data = new X509DataBuilder().buildObject();
        X509CertificateBuilder certBuilder = new X509CertificateBuilder();
        org.opensaml.xmlsec.signature.X509Certificate samlCert = certBuilder.buildObject();
        samlCert.setValue(Base64.getEncoder().encodeToString(cert.getEncoded()));
        x509Data.getX509Certificates().add(samlCert);

        KeyInfo keyInfo = new KeyInfoBuilder().buildObject();
        keyInfo.getX509Datas().add(x509Data);

        return keyInfo;
    }

    static String toString(org.w3c.dom.Element element) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        tf.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }
}
