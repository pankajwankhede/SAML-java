// utils/normalizeSamlResponse.js

/**
 * Normalize a raw SAMLResponse string coming from backend
 * - Removes newlines / spaces
 * - Strips extra wrapping quotes
 * - Ensures it's a plain string
 */
export function normalizeSamlResponse(raw) {
  if (!raw) return "";

  return String(raw)
    .trim()
    .replace(/\r?\n|\r/g, "")  // remove line breaks
    .replace(/\s+/g, "")       // remove spaces
    .replace(/^"+|"+$/g, "");  // strip leading/trailing quotes
}
