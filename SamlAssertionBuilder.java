import org.opensaml.saml.saml2.core.*;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.opensaml.saml.saml2.core.impl.*;

import java.time.Instant;
import java.util.UUID;

import org.opensaml.core.xml.schema.XSString;

public class SamlAssertionBuilder {

    public static Assertion buildAssertion(String issuerValue, String subjectEmail) {
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        Assertion assertion = (Assertion) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME)
                .buildObject(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID("_" + UUID.randomUUID());
        assertion.setIssueInstant(Instant.now());

        Issuer issuer = (Issuer) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME)
                .buildObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issuerValue);
        assertion.setIssuer(issuer);

        NameID nameID = (NameID) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME)
                .buildObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat(NameID.EMAIL);
        nameID.setValue(subjectEmail);

        Subject subject = (Subject) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME)
                .buildObject(Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID(nameID);
        assertion.setSubject(subject);

        AttributeStatement attrStmt = (AttributeStatement) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME)
                .buildObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
        attrStmt.getAttributes().add(createAttribute("email", subjectEmail));
        attrStmt.getAttributes().add(createAttribute("name", "John Doe"));

        assertion.getAttributeStatements().add(attrStmt);
        return assertion;
    }

    private static Attribute createAttribute(String name, String value) {
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        Attribute attribute = (Attribute) builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME)
                .buildObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(name);
        XSString attrValue = (XSString) builderFactory.getBuilder(XSString.TYPE_NAME)
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrValue.setValue(value);
        attribute.getAttributeValues().add(attrValue);
        return attribute;
    }
}
