package rw.itegeko.legal.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "legal_categories", schema = "legal")
public class LegalCategory {
    @Id
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String status;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
}
