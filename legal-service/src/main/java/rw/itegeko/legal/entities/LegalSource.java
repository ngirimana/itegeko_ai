package rw.itegeko.legal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "legal_sources", schema = "legal")
public class LegalSource {
    @Id
    private UUID id;
    private String name;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "official_url")
    private String officialUrl;

    private String institution;

    @Column(name = "trust_level")
    private String trustLevel;

    private boolean verified;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSourceType() { return sourceType; }
    public String getOfficialUrl() { return officialUrl; }
    public String getInstitution() { return institution; }
    public String getTrustLevel() { return trustLevel; }
    public boolean isVerified() { return verified; }
}
