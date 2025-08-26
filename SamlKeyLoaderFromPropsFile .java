import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class SamlKeyLoader {

    @Value("${saml.idp.certificate}")
    private String pemCertificate;

    @Value("${saml.idp.private-key}")
    private String pemPrivateKey;

    public X509Certificate getCertificate() throws Exception {
        String normalizedCert = pemCertificate.replace("\\n", "\n");
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (ByteArrayInputStream in =
                     new ByteArrayInputStream(normalizedCert.getBytes(StandardCharsets.UTF_8))) {
            return (X509Certificate) factory.generateCertificate(in);
        }
    }

    public PrivateKey getPrivateKey() throws Exception {
        String normalizedKey = pemPrivateKey
                .replace("\\n", "\n")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // remove whitespace

        byte[] decoded = Base64.getDecoder().decode(normalizedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // or EC depending on key type
        return keyFactory.generatePrivate(keySpec);
    }
}
