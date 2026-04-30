CREATE TABLE IF NOT EXISTS legal.scraped_legal_documents (
  source_url TEXT PRIMARY KEY,
  document_id UUID UNIQUE NOT NULL REFERENCES legal.legal_documents(id) ON DELETE CASCADE,
  source_name TEXT NOT NULL DEFAULT 'RLRC',
  source_path TEXT,
  scraper_scope TEXT NOT NULL DEFAULT 'custom',
  document_type TEXT NOT NULL DEFAULT 'other',
  content_hash TEXT NOT NULL,
  article_count INT NOT NULL DEFAULT 0,
  first_scraped_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_scraped_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_scraped_legal_documents_last_scraped
  ON legal.scraped_legal_documents(last_scraped_at);
