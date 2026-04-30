package rw.itegeko.legal.services;

import rw.itegeko.legal.payloads.internal.ArticlesForIndexingResponse;
import rw.itegeko.legal.payloads.internal.EmbeddingBatchUpsertRequest;
import rw.itegeko.legal.payloads.internal.IndexResponse;
import rw.itegeko.legal.payloads.internal.KeywordSearchRequest;
import rw.itegeko.legal.payloads.internal.LegalRetrievalResponse;
import rw.itegeko.legal.payloads.internal.VectorSearchRequest;

public interface InternalAiDataService {
    ArticlesForIndexingResponse listArticlesForIndexing();
    IndexResponse upsertEmbeddings(EmbeddingBatchUpsertRequest request);
    LegalRetrievalResponse searchByVector(VectorSearchRequest request);
    LegalRetrievalResponse fallbackKeywordSearch(KeywordSearchRequest request);
}
