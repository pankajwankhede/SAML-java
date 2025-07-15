package com.example.saml;

import jakarta.annotation.PostConstruct;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestUnmarshaller;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.InflaterInputStream;

@SpringBootApplication
@RestController
public class SamlMetadataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamlMetadataApplication.class, args);
    }

    // === STEP 1: Init OpenSAML ===
    @PostConstruct
    public void initOpenSAML() throws Exception {
        InitializationService.initialize();
    }

    // === STEP 2: Serve Metadata ===
    @GetMapping(value = "/saml/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getMetadata() throws Exception {
        String entityId = "https://idp.example.com";
        String ssoUrl = "https://idp.example.com/saml/login";
        String certBase64 = loadCertificateBase64("idp-cert.pem");

        String metadata = """
            <EntityDescriptor xmlns="urn:oasis:names:tc:SAML:2.0:metadata"
                              xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                              entityID="%s">
              <IDPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol"
                                WantAuthnRequestsSigned="false">

                <KeyDescriptor use="signing">
                  <ds:KeyInfo>
                    <ds:X509Data>
                      <ds:X509Certificate>%s</ds:X509Certificate>
                    </ds:X509Data>
                  </ds:KeyInfo>
                </KeyDescriptor>

                <SingleSignOnService
                  Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
                  Location="%s" />

                <NameIDFormat>
                  urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress
                </NameIDFormat>

              </IDPSSODescriptor>
            </EntityDescriptor>
        """.formatted(entityId, certBase64, ssoUrl);

        return ResponseEntity.ok(metadata);
    }

    // === STEP 3: Handle AuthnRequest ===
    @GetMapping("/saml/login")
    public ResponseEntity<String> handleSamlLogin(@RequestParam("SAMLRequest") String samlRequestEncoded) throws Exception {
        String xml = decodeSAMLRequest(samlRequestEncoded);
        AuthnRequest authnRequest = parseAuthnRequest(xml);

        // Just logging key info (no validation or signing yet)
        String info = """
                <h2>SAML AuthnRequest Received</h2>
                <ul>
                  <li>Issuer: %s</li>
                  <li>Destination: %s</li>
                  <li>AssertionConsumerServiceURL: %s</li>
                </ul>
                """.formatted(
                authnRequest.getIssuer().getValue(),
                authnRequest.getDestination(),
                authnRequest.getAssertionConsumerServiceURL()
        );

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(info);
    }

    // === Decode base64 + inflate ===
    private String decodeSAMLRequest(String encoded) throws Exception {
        byte[] compressed = Base64.getDecoder().decode(URLDecoder.decode(encoded, StandardCharsets.UTF_8));
        try (InputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // === Parse SAML XML into AuthnRequest ===
    private AuthnRequest parseAuthnRequest(String xml) throws Exception {
        var dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        var docBuilder = dbFactory.newDocumentBuilder();
        var document = docBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        var element = document.getDocumentElement();

        AuthnRequestUnmarshaller unmarshaller = (AuthnRequestUnmarshaller)
                XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(element);

        XMLObject xmlObj = unmarshaller.unmarshall(element);
        return (AuthnRequest) xmlObj;
    }

    private String loadCertificateBase64(String fileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            String pem = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return pem.replace("-----BEGIN CERTIFICATE-----", "")
                      .replace("-----END CERTIFICATE-----", "")
                      .replaceAll("\\s+", "");
        }
    }
}
