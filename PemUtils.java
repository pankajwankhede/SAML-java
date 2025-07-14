import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class PemUtils {
    public static PrivateKey loadPrivateKey(String path) throws Exception {
        try (InputStream is = PemUtils.class.getClassLoader().getResourceAsStream(path);
             PEMParser parser = new PEMParser(new InputStreamReader(is))) {

            Object obj = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (obj instanceof PEMKeyPair keyPair) {
                KeyPair kp = converter.getKeyPair(keyPair);
                return kp.getPrivate();
            }
            throw new IllegalArgumentException("Invalid private key format");
        }
    }

    public static X509Certificate loadCertificate(String path) throws Exception {
        try (InputStream is = PemUtils.class.getClassLoader().getResourceAsStream(path)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        }
    }
}
