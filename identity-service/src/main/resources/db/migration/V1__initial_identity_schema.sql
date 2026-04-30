CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  full_name VARCHAR(180) NOT NULL,
  email VARCHAR(220) UNIQUE NOT NULL,
  phone VARCHAR(50),
  external_auth_id TEXT,
  status VARCHAR(40) NOT NULL DEFAULT 'active',
  last_login_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.roles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(80) UNIQUE NOT NULL,
  description TEXT
);

CREATE TABLE identity.user_roles (
  user_id UUID NOT NULL REFERENCES identity.users(id),
  role_id UUID NOT NULL REFERENCES identity.roles(id),
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  assigned_by UUID REFERENCES identity.users(id),
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE identity.organizations (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(220) NOT NULL,
  type VARCHAR(80),
  registration_number VARCHAR(120),
  country VARCHAR(80) NOT NULL DEFAULT 'Rwanda',
  status VARCHAR(40) NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.organization_members (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  organization_id UUID NOT NULL REFERENCES identity.organizations(id),
  user_id UUID NOT NULL REFERENCES identity.users(id),
  org_role VARCHAR(80) NOT NULL,
  status VARCHAR(40) NOT NULL DEFAULT 'active',
  joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (organization_id, user_id)
);

CREATE TABLE identity.user_activities (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID REFERENCES identity.users(id),
  action VARCHAR(160) NOT NULL,
  resource_type VARCHAR(120),
  resource_id TEXT,
  metadata JSONB,
  ip_address TEXT,
  user_agent TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.audit_logs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  actor_user_id UUID REFERENCES identity.users(id),
  organization_id UUID REFERENCES identity.organizations(id),
  action VARCHAR(160) NOT NULL,
  entity_type VARCHAR(120) NOT NULL,
  entity_id TEXT,
  ip_address TEXT,
  user_agent TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_identity_users_email ON identity.users(email);
CREATE INDEX idx_identity_user_activities_user_created ON identity.user_activities(user_id, created_at DESC);
CREATE INDEX idx_identity_audit_actor_created ON identity.audit_logs(actor_user_id, created_at DESC);
