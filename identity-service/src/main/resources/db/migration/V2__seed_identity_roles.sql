INSERT INTO identity.roles (id, name, description)
VALUES
  ('10000000-0000-0000-0000-000000000001', 'PUBLIC_USER', 'Can search public legal content and ask public legal information questions.'),
  ('10000000-0000-0000-0000-000000000002', 'ADMIN', 'Can manage platform configuration and legal source administration.'),
  ('10000000-0000-0000-0000-000000000003', 'LEGAL_REVIEWER', 'Can review and verify legal content status.'),
  ('10000000-0000-0000-0000-000000000004', 'LAWYER', 'Can receive human legal review workflows.'),
  ('10000000-0000-0000-0000-000000000005', 'ORG_ADMIN', 'Can manage organization membership and access.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO identity.users (id, full_name, email, external_auth_id, status)
VALUES
  ('20000000-0000-0000-0000-000000000001', 'System Admin', 'admin@itegeko.local', 'admin@itegeko.local', 'active'),
  ('20000000-0000-0000-0000-000000000002', 'Legal Reviewer', 'reviewer@itegeko.local', 'reviewer@itegeko.local', 'active')
ON CONFLICT (email) DO NOTHING;

INSERT INTO identity.user_roles (user_id, role_id)
VALUES
  ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003')
ON CONFLICT (user_id, role_id) DO NOTHING;
