import React, { useEffect } from "react";

export default function SamlPostToAcs({ samlResponse, relayState, acsUrl }) {
  useEffect(() => {
    document.forms[0].submit();
  }, []);

  return (
    <form action={acsUrl} method="post">
      <input type="hidden" name="SAMLResponse" value={samlResponse} />
      {relayState && <input type="hidden" name="RelayState" value={relayState} />}
      <noscript>
        <p>JavaScript is disabled. Click the button below to continue.</p>
        <input type="submit" value="Continue" />
      </noscript>
    </form>
  );
}


============using iframe ==================

  import React, { useEffect, useRef, useState } from "react";

const SamlPostIframe = ({ acsUrl, samlResponse, relayState }) => {
  const iframeRef = useRef(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (iframeRef.current && samlResponse) {
      const iframeDoc = iframeRef.current.contentDocument || iframeRef.current.contentWindow.document;

      // Reset iframe
      iframeDoc.open();
      iframeDoc.write("<html><body></body></html>");
      iframeDoc.close();

      // Create a form inside iframe
      const form = iframeDoc.createElement("form");
      form.method = "POST";
      form.action = acsUrl;

      // Add SAMLResponse input
      const samlInput = iframeDoc.createElement("input");
      samlInput.type = "hidden";
      samlInput.name = "SAMLResponse";
      samlInput.value = samlResponse;
      form.appendChild(samlInput);

      // Optional RelayState
      if (relayState) {
        const relayInput = iframeDoc.createElement("input");
        relayInput.type = "hidden";
        relayInput.name = "RelayState";
        relayInput.value = relayState;
        form.appendChild(relayInput);
      }

      iframeDoc.body.appendChild(form);

      // Auto-submit form
      form.submit();

      // Show loader for 2s (simulate async POST)
      setTimeout(() => setLoading(false), 2000);
    }
  }, [acsUrl, samlResponse, relayState]);

  return (
    <div style={{ textAlign: "center", marginTop: "20px" }}>
      {loading && <p>🔒 Signing you in securely...</p>}
      <iframe
        ref={iframeRef}
        title="SAML Post Iframe"
        style={{
          width: "100%",
          height: "400px",
          border: "1px solid #ccc",
          borderRadius: "8px",
          marginTop: "10px",
        }}
      />
    </div>
  );
};

export default SamlPostIframe;

