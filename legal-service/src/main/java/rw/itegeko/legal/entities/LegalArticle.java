package rw.itegeko.legal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "legal_articles", schema = "legal")
public class LegalArticle {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private LegalDocument document;

    @Column(name = "article_number")
    private String articleNumber;

    @Column(name = "article_title")
    private String articleTitle;

    @Column(name = "article_text", nullable = false)
    private String articleText;

    private String language;
    private String status;

    public UUID getId() { return id; }
    public LegalDocument getDocument() { return document; }
    public String getArticleNumber() { return articleNumber; }
    public String getArticleTitle() { return articleTitle; }
    public String getArticleText() { return articleText; }
    public String getLanguage() { return language; }
    public String getStatus() { return status; }
}
