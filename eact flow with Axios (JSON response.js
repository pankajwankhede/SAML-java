
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
