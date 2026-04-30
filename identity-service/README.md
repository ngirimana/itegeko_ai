# 🔐 Itegeko Identity Service

The **Identity Service** manages user profiles, roles, organizations, and security audits for the Itegeko AI platform. It integrates with Keycloak for authentication and provides a rich set of management features for administrative users.

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.3.7
- **Language**: Java 21
- **Database**: PostgreSQL
- **Migrations**: Flyway
- **Security**: Spring Security + OAuth2 Resource Server (Keycloak integration)

## 📂 Internal Structure

```text
src/main/java/rw/itegeko/identity/
  ├── config/        # Security, CORS, and Audit configs
  ├── constants/     # Reusable strings and API paths
  ├── controllers/   # REST Endpoints (Users, Orgs, Audit)
  ├── entities/      # JPA entities (User, Role, Organization, AuditLog)
  ├── exceptions/    # Global exception handling
  ├── payloads/      # Request/Response DTOs
  ├── repositories/  # Spring Data JPA repositories
  └── services/      # Business logic (User mgmt, RBAC sync, Audit logging)
```

## 🛠️ Key Features

- **User Profiles**: Manage personal information and preferences.
- **RBAC**: Sync and manage roles from Keycloak.
- **Audit Logs**: Immutable tracking of sensitive actions across the system.
- **User Activity**: Track search history and document interaction (scoped by user or admin).
- **Organizations**: Manage multi-tenant memberships and collaborative access.

## 🛠️ Key Endpoints

- `GET /identity-api/v1/users/me`: Fetch current authenticated user profile.
- `GET /identity-api/v1/activity`: Fetch user's own activity history.
- `GET /identity-api/v1/admin/audit`: (Admin only) Global audit log view.
- `PUT /identity-api/v1/users/{id}`: Update user profile data.

## 🧪 Running Tests

```bash
mvn test
```

## 🏗️ Building & Running

**Docker (Recommended)**:
```bash
docker compose up identity-service --build
```

**Local**:
Make sure you have a PostgreSQL instance and Keycloak running.
```bash
mvn spring-boot:run
```
