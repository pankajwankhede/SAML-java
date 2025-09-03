import { useCallback } from "react";
import qs from "qs";
import axios from "axios";

export function useSamlPost(acsUrl) {
  // 1. Hidden Form Submit
  const postWithForm = useCallback((samlResponse, relayState) => {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = acsUrl;

    const samlInput = document.createElement("input");
    samlInput.type = "hidden";
    samlInput.name = "SAMLResponse";
    samlInput.value = String(samlResponse).trim();
    form.appendChild(samlInput);

    if (relayState) {
      const relayInput = document.createElement("input");
      relayInput.type = "hidden";
      relayInput.name = "RelayState";
      relayInput.value = String(relayState).trim();
      form.appendChild(relayInput);
    }

    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
  }, [acsUrl]);

  // 2. Iframe Auto-POST
  const postWithIframe = useCallback((samlResponse, relayState) => {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = acsUrl;
    form.target = "samlIframe";

    const samlInput = document.createElement("input");
    samlInput.type = "hidden";
    samlInput.name = "SAMLResponse";
    samlInput.value = String(samlResponse).trim();
    form.appendChild(samlInput);

    if (relayState) {
      const relayInput = document.createElement("input");
      relayInput.type = "hidden";
      relayInput.name = "RelayState";
      relayInput.value = String(relayState).trim();
      form.appendChild(relayInput);
    }

    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
  }, [acsUrl]);

  // 3. Axios POST
  const postWithAxios = useCallback(async (samlResponse, relayState) => {
    return axios.post(acsUrl, qs.stringify({
      SAMLResponse: String(samlResponse).trim(),
      RelayState: relayState ? String(relayState).trim() : undefined
    }), {
      headers: { "Content-Type": "application/x-www-form-urlencoded" }
    });
  }, [acsUrl]);

  // 4. New Window Form
  const postWithWindow = useCallback((samlResponse, relayState) => {
    const newWindow = window.open("", "_blank");
    newWindow.document.write(`
      <form method="POST" action="${acsUrl}">
        <input type="hidden" name="SAMLResponse" value="${String(samlResponse).trim()}" />
        ${relayState ? `<input type="hidden" name="RelayState" value="${String(relayState).trim()}" />` : ""}
      </form>
      <script>document.forms[0].submit();</script>
    `);
  }, [acsUrl]);

  return { postWithForm, postWithIframe, postWithAxios, postWithWindow };
}


================
  import React from "react";
import { useSamlPost } from "./useSamlPost";

export default function SamlTester({ acsUrl, samlResponse, relayState }) {
  const { postWithForm, postWithIframe, postWithAxios, postWithWindow } = useSamlPost(acsUrl);

  return (
    <div className="space-y-4">
      <button onClick={() => postWithForm(samlResponse, relayState)}>
        Post with Hidden Form
      </button>

      <button onClick={() => postWithIframe(samlResponse, relayState)}>
        Post with Iframe
      </button>
      <iframe name="samlIframe" style={{ display: "none" }} title="SAML ACS" />

      <button onClick={() => postWithAxios(samlResponse, relayState)}>
        Post with Axios (x-www-form-urlencoded)
      </button>

      <button onClick={() => postWithWindow(samlResponse, relayState)}>
        Post with New Window
      </button>
    </div>
  );
}

===========
  <SamlTester 
  acsUrl="https://idp.example.com/saml/acs"
  samlResponse="PHNhbWxwOlJlc3BvbnNlIHhtb..."
  relayState="relay123"
/>

  ===axios.get("/api/saml/response").then(res => {
  let samlResp = res.data.samlResponse;

  // Force string + cleanup
  samlResp = String(samlResp).replace(/\s+/g, "").replace(/^"+|"+$/g, "");

  setSamlResponse(samlResp);
});

