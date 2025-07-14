import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.security.credential.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class SamlResponseSender {

    public static String buildSignedResponse(String acsUrl, Assertion assertion,
                                             PrivateKey privateKey, X509Certificate cert) throws Exception {

        ResponseBuilder responseBuilder = new ResponseBuilder();
        Response response = responseBuilder.buildObject();
        response.setID("_" + UUID.randomUUID());
        response.setIssueInstant(Instant.now());
        response.setDestination(acsUrl);
        response.getAssertions().add(assertion);

        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        statusCode.setValue(StatusCode.SUCCESS);
        Status status = new StatusBuilder().buildObject();
        status.setStatusCode(statusCode);
        response.setStatus(status);

        Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        BasicX509Credential credential = new BasicX509Credential(cert, privateKey);
        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        response.setSignature(signature);

        Marshaller marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(response);
        marshaller.marshall(response);
        Signer.signObject(signature);

        String xml =  XmlUtils.toString(response); //XMLObjectSupport.nodeToString(response.getDOM());
        return Base64.getEncoder().encodeToString(xml.getBytes());
    }
}
