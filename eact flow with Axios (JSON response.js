
import axios from "axios";
import { useEffect, useState } from "react";
import SamlIframePost from "./SamlIframePost";

export default function SamlConsumer() {
  const [samlResponse, setSamlResponse] = useState(null);

  useEffect(() => {
    axios
      .get("/api/saml/response") // returns { samlResponse: "..." }
      .then((res) => {
        const raw = res.data.samlResponse;
        const cleaned = cleanBase64(raw);
        console.log("✅ Cleaned Base64:", cleaned.substring(0, 100));
        setSamlResponse(cleaned);
      })
      .catch((err) => console.error("Error fetching SAMLResponse", err));
  }, []);

  return (
    <>
      {samlResponse && (
        <SamlIframePost
          acsUrl="https://idp.example.com/acs"
          samlResponse={samlResponse}
          relayState="myRelayState"
        />
      )}
    </>
  );
}

function cleanBase64(b64) {
  return b64
    .replace(/\s/g, "")
    .replace(/-/g, "+")
    .replace(/_/g, "/");
}

==============exmp2=================
  import React, { useEffect, useState } from "react";
import axios from "axios";

export default function SamlAutoPost({ acsUrl, relayState }) {
  const [samlResponse, setSamlResponse] = useState(null);

  useEffect(() => {
    // 1. Fetch SAMLResponse from backend
    axios.get("/api/saml/response", { responseType: "text" })
      .then(res => {
        setSamlResponse(res.data);
      })
      .catch(err => {
        console.error("Error fetching SAMLResponse:", err);
      });
  }, []);

  useEffect(() => {
    // 2. Once samlResponse is ready, auto-submit form
    if (samlResponse) {
      const form = document.getElementById("samlForm");
      if (form) form.submit();
    }
  }, [samlResponse]);

  return (
    <>
      <iframe name="samlFrame" style={{ display: "none" }} />

      <form
        id="samlForm"
        method="POST"
        action={acsUrl}
        target="samlFrame"
      >
        <input type="hidden" name="SAMLResponse" value={samlResponse || ""} />
        {relayState && <input type="hidden" name="RelayState" value={relayState} />}
      </form>
    </>
  );
}

