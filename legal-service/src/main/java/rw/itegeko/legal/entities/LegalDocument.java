package rw.itegeko.legal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "legal_documents", schema = "legal")
public class LegalDocument {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private LegalCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private LegalSource source;

    private String title;

    @Column(name = "law_number")
    private String lawNumber;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    private String language;
    private String status;

    @Column(name = "source_url")
    private String sourceUrl;

    public UUID getId() { return id; }
    public LegalCategory getCategory() { return category; }
    public LegalSource getSource() { return source; }
    public String getTitle() { return title; }
    public String getLawNumber() { return lawNumber; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public String getLanguage() { return language; }
    public String getStatus() { return status; }
    public String getSourceUrl() { return sourceUrl; }
}
