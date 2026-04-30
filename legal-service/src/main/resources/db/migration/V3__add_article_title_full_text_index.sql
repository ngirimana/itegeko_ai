CREATE INDEX idx_legal_articles_title_fts
  ON legal.legal_articles
  USING gin(to_tsvector('english', coalesce(article_title, '')));
