import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.config.InitializationService;

public class Main {
    public static void main(String[] args) throws Exception {
         InitializationService.initialize();
        String entityId = "urn:auth0";
        String acsUrl = "https://your-tenant.auth0.com/samlp/client12345";

        PrivateKey privateKey = PemUtils.loadPrivateKey("private-key.pem");
        X509Certificate cert = PemUtils.loadCertificate("public-cert.pem");

        Assertion assertion = SamlAssertionBuilder.buildAssertion(entityId, "user@example.com");
        String samlResponse = SamlResponseSender.buildSignedResponse(acsUrl, assertion, privateKey, cert);

        System.out.println("<form method='POST' action='" + acsUrl + "'>");
        System.out.println("<input type='hidden' name='SAMLResponse' value='" + samlResponse + "'/>");
        System.out.println("<input type='submit' value='Login via IdP'/>");
        System.out.println("</form>");
    }
}
