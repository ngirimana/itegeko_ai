CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;
CREATE SCHEMA IF NOT EXISTS legal;

CREATE TABLE legal.legal_categories (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(150) NOT NULL,
  slug VARCHAR(180) UNIQUE NOT NULL,
  parent_id UUID REFERENCES legal.legal_categories(id),
  description TEXT,
  status VARCHAR(40) NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.legal_sources (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(220) NOT NULL,
  source_type VARCHAR(80) NOT NULL,
  official_url TEXT,
  institution VARCHAR(200),
  trust_level VARCHAR(50) NOT NULL DEFAULT 'official',
  verification_notes TEXT,
  verified BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.legal_documents (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  category_id UUID REFERENCES legal.legal_categories(id),
  source_id UUID REFERENCES legal.legal_sources(id),
  title TEXT NOT NULL,
  law_number VARCHAR(100),
  gazette_number VARCHAR(100),
  publication_date DATE,
  effective_date DATE,
  language VARCHAR(30) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'unverified',
  source_url TEXT,
  file_uri TEXT,
  last_verified_date DATE,
  extraction_status VARCHAR(40) NOT NULL DEFAULT 'pending',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.legal_articles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  document_id UUID NOT NULL REFERENCES legal.legal_documents(id),
  chapter TEXT,
  section TEXT,
  article_number VARCHAR(80),
  article_title TEXT,
  article_text TEXT NOT NULL,
  language VARCHAR(30) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'unverified',
  order_index INT,
  source_page_start INT,
  source_page_end INT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.article_embeddings (
  article_id UUID PRIMARY KEY REFERENCES legal.legal_articles(id),
  embedding_model VARCHAR(160) NOT NULL,
  embedding vector(384),
  chunk_text TEXT NOT NULL,
  chunk_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.ai_questions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID,
  question_text TEXT NOT NULL,
  language VARCHAR(30) NOT NULL DEFAULT 'English',
  legal_category_id UUID REFERENCES legal.legal_categories(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.ai_answers (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  question_id UUID UNIQUE NOT NULL REFERENCES legal.ai_questions(id),
  answer_text TEXT NOT NULL,
  answer_language VARCHAR(30) NOT NULL DEFAULT 'English',
  model_name VARCHAR(160) NOT NULL,
  confidence_level VARCHAR(40) NOT NULL,
  disclaimer_included BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE legal.answer_sources (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  answer_id UUID NOT NULL REFERENCES legal.ai_answers(id),
  article_id UUID REFERENCES legal.legal_articles(id),
  source_rank INT NOT NULL,
  quoted_excerpt TEXT,
  relevance_score NUMERIC(8, 5),
  page_reference VARCHAR(80)
);

CREATE TABLE legal.uploaded_documents (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  owner_user_id UUID,
  organization_id UUID,
  file_name TEXT NOT NULL,
  file_uri TEXT,
  file_type VARCHAR(80),
  file_size INT,
  file_hash TEXT,
  document_type VARCHAR(80),
  consent_given BOOLEAN NOT NULL DEFAULT false,
  retention_status VARCHAR(80) NOT NULL DEFAULT 'standard',
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_legal_documents_category ON legal.legal_documents(category_id);
CREATE INDEX idx_legal_documents_status ON legal.legal_documents(status);
CREATE INDEX idx_legal_articles_document ON legal.legal_articles(document_id);
CREATE INDEX idx_legal_articles_article_number ON legal.legal_articles(article_number);
CREATE INDEX idx_uploaded_documents_owner ON legal.uploaded_documents(owner_user_id, organization_id);
CREATE INDEX idx_article_embeddings_vector_cosine ON legal.article_embeddings
  USING ivfflat (embedding vector_cosine_ops)
  WITH (lists = 100)
  WHERE embedding IS NOT NULL;
CREATE INDEX idx_legal_documents_title_fts ON legal.legal_documents USING gin(to_tsvector('english', coalesce(title, '')));
CREATE INDEX idx_legal_articles_text_fts ON legal.legal_articles USING gin(to_tsvector('english', coalesce(article_text, '')));
