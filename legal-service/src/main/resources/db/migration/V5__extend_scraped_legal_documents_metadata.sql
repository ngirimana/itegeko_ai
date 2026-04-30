ALTER TABLE legal.scraped_legal_documents
  ADD COLUMN IF NOT EXISTS source_path TEXT;

ALTER TABLE legal.scraped_legal_documents
  ADD COLUMN IF NOT EXISTS scraper_scope TEXT NOT NULL DEFAULT 'custom';

ALTER TABLE legal.scraped_legal_documents
  ADD COLUMN IF NOT EXISTS document_type TEXT NOT NULL DEFAULT 'other';

CREATE INDEX IF NOT EXISTS idx_scraped_legal_documents_document_type
  ON legal.scraped_legal_documents(document_type);
