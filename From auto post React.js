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
