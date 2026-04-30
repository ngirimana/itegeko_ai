package rw.itegeko.identity.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "identity")
public class AppUser {
    @Id
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    private String email;
    private String phone;

    @Column(name = "external_auth_id")
    private String externalAuthId;

    private String status;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getExternalAuthId() { return externalAuthId; }
    public String getStatus() { return status; }
    public OffsetDateTime getLastLoginAt() { return lastLoginAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
