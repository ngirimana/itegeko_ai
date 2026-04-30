package rw.itegeko.legal.repositories;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.itegeko.legal.entities.LegalArticle;

public interface LegalArticleRepository extends JpaRepository<LegalArticle, UUID> {
    @Query(
        value = """
            SELECT
              a.id AS id,
              a.article_number AS "articleNumber",
              a.article_title AS "articleTitle",
              a.article_text AS "articleText",
              a.language AS language,
              a.status AS status,
              0.0 AS "relevanceScore",
              d.id AS "documentId",
              d.title AS "documentTitle",
              d.status AS "documentStatus",
              d.source_url AS "sourceUrl",
              d.publication_date AS "publicationDate",
              c.id AS "categoryId",
              c.name AS "categoryName",
              c.slug AS "categorySlug",
              s.id AS "sourceId",
              s.name AS "sourceName",
              s.source_type AS "sourceType",
              s.official_url AS "officialUrl",
              s.verified AS "sourceVerified"
            FROM legal.legal_articles a
            JOIN legal.legal_documents d ON d.id = a.document_id
            LEFT JOIN legal.legal_categories c ON c.id = d.category_id
            LEFT JOIN legal.legal_sources s ON s.id = d.source_id
            ORDER BY d.publication_date DESC NULLS LAST, d.title, a.order_index NULLS LAST
            """,
        countQuery = "SELECT count(*) FROM legal.legal_articles",
        nativeQuery = true
    )
    Page<LegalArticleSearchRow> findAllSummaries(Pageable pageable);

    @Query(
        value = """
            SELECT
              a.id AS id,
              a.article_number AS "articleNumber",
              a.article_title AS "articleTitle",
              a.article_text AS "articleText",
              a.language AS language,
              a.status AS status,
              ts_rank_cd(
                to_tsvector('english', coalesce(a.article_text, '') || ' ' || coalesce(a.article_title, '')),
                websearch_to_tsquery('english', :query)
              ) + ts_rank_cd(
                to_tsvector('english', coalesce(d.title, '')),
                websearch_to_tsquery('english', :query)
              ) AS "relevanceScore",
              d.id AS "documentId",
              d.title AS "documentTitle",
              d.status AS "documentStatus",
              d.source_url AS "sourceUrl",
              d.publication_date AS "publicationDate",
              c.id AS "categoryId",
              c.name AS "categoryName",
              c.slug AS "categorySlug",
              s.id AS "sourceId",
              s.name AS "sourceName",
              s.source_type AS "sourceType",
              s.official_url AS "officialUrl",
              s.verified AS "sourceVerified"
            FROM legal.legal_articles a
            JOIN legal.legal_documents d ON d.id = a.document_id
            LEFT JOIN legal.legal_categories c ON c.id = d.category_id
            LEFT JOIN legal.legal_sources s ON s.id = d.source_id
            WHERE to_tsvector('english', coalesce(a.article_text, ''))
                    @@ websearch_to_tsquery('english', :query)
               OR to_tsvector('english', coalesce(a.article_title, ''))
                    @@ websearch_to_tsquery('english', :query)
               OR to_tsvector('english', coalesce(d.title, ''))
                    @@ websearch_to_tsquery('english', :query)
               OR lower(a.article_text) LIKE lower(concat('%', :query, '%'))
               OR lower(a.article_title) LIKE lower(concat('%', :query, '%'))
               OR lower(d.title) LIKE lower(concat('%', :query, '%'))
            ORDER BY "relevanceScore" DESC, d.publication_date DESC NULLS LAST, a.order_index NULLS LAST
            """,
        countQuery = """
            SELECT count(*)
            FROM legal.legal_articles a
            JOIN legal.legal_documents d ON d.id = a.document_id
            WHERE to_tsvector('english', coalesce(a.article_text, ''))
                    @@ websearch_to_tsquery('english', :query)
               OR to_tsvector('english', coalesce(a.article_title, ''))
                    @@ websearch_to_tsquery('english', :query)
               OR to_tsvector('english', coalesce(d.title, ''))
                    @@ websearch_to_tsquery('english', :query)
               OR lower(a.article_text) LIKE lower(concat('%', :query, '%'))
               OR lower(a.article_title) LIKE lower(concat('%', :query, '%'))
               OR lower(d.title) LIKE lower(concat('%', :query, '%'))
            """,
        nativeQuery = true
    )
    Page<LegalArticleSearchRow> keywordSearch(@Param("query") String query, Pageable pageable);
}
