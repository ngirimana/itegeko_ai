export type LegalDocumentSummary = {
  id?: string;
  title?: string;
  status?: string;
  sourceUrl?: string;
  publicationDate?: string;
  category?: {
    id?: string;
    name?: string;
    slug?: string;
  };
  source?: {
    id?: string;
    name?: string;
    sourceType?: string;
    officialUrl?: string;
    verified?: boolean;
  };
};

export type LegalArticleResult = {
  id: string;
  articleNumber?: string;
  articleTitle?: string;
  articleText?: string;
  language?: string;
  status?: string;
  relevanceScore?: number;
  document?: LegalDocumentSummary;
};

export type LegalSearchResponse = {
  results: LegalArticleResult[];
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
};

export type LegalCatalogResponse = {
  categories: Array<{
    id: string;
    name: string;
    slug?: string;
    description?: string;
    status?: string;
  }>;
  sources: Array<{
    id: string;
    name: string;
    sourceType?: string;
    officialUrl?: string;
    institution?: string;
    trustLevel?: string;
    verified?: boolean;
  }>;
};

export type AskQuestionRequest = {
  question: string;
  categoryId?: string;
};

export type AskQuestionResponse = {
  supported?: boolean;
  answer?: {
    answerText?: string;
    confidenceLevel?: string;
  };
  answerText?: string;
  sources?: unknown[];
};

export type CreateLegalArticleRequest = {
  articleNumber: string;
  articleTitle?: string;
  articleText: string;
};

export type CreateLegalDocumentRequest = {
  title: string;
  lawNumber?: string;
  publicationDate?: string;
  language: string;
  status: string;
  sourceUrl?: string;
  categoryId?: string;
  sourceId?: string;
  articles: CreateLegalArticleRequest[];
};

export type CreateLegalDocumentResponse = {
  documentId: string;
  articleIds: string[];
  indexedArticles: number;
  indexingStatus: string;
};
