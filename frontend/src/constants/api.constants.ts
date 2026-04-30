export const LEGAL_API_PATHS = {
  catalog: "/api/catalog",
  ask: "/api/ask",
  search: "/api/search",
  adminLegalDocuments: "/api/admin/legal-documents"
} as const;

export const HTTP_HEADERS = {
  json: { "content-type": "application/json" }
} as const;
