INSERT INTO legal.legal_categories (id, name, slug, description, status)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'Labour', 'labour', 'Employment rights, duties, contracts, leave, termination, and workplace obligations.', 'active'),
  ('22222222-2222-2222-2222-222222222222', 'Business and Company', 'business-company', 'Company registration, corporate duties, contracts, and business compliance.', 'active'),
  ('33333333-3333-3333-3333-333333333333', 'Data Protection', 'data-protection', 'Personal data processing, consent, security, and privacy rights.', 'active')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO legal.legal_sources (id, name, source_type, official_url, institution, trust_level, verified)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Official Gazette of the Republic of Rwanda', 'official_gazette', 'https://www.minijust.gov.rw', 'MINIJUST', 'official', true),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Rwanda Law Reform Commission', 'official_portal', 'https://www.rlrc.gov.rw', 'RLRC', 'official', true),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Rwanda Information Society Authority', 'regulator', 'https://www.risa.gov.rw', 'RISA', 'trusted', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO legal.legal_documents (
  id, category_id, source_id, title, law_number, publication_date, effective_date,
  language, status, source_url, last_verified_date, extraction_status
)
VALUES
  ('dddddddd-dddd-dddd-dddd-dddddddddd01', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Labour Law - Demo Extract', 'N/A-DEMO', '2023-01-01', '2023-01-01', 'English', 'verified', 'https://www.minijust.gov.rw', '2026-04-29', 'completed'),
  ('dddddddd-dddd-dddd-dddd-dddddddddd02', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Companies Law - Demo Extract', 'N/A-DEMO', '2022-01-01', '2022-01-01', 'English', 'active', 'https://www.rlrc.gov.rw', '2026-04-29', 'completed'),
  ('dddddddd-dddd-dddd-dddd-dddddddddd03', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Data Protection Law - Demo Extract', 'N/A-DEMO', '2021-01-01', '2021-01-01', 'English', 'amended', 'https://www.risa.gov.rw', '2026-04-29', 'completed')
ON CONFLICT (id) DO NOTHING;

INSERT INTO legal.legal_articles (
  id, document_id, chapter, section, article_number, article_title, article_text,
  language, status, order_index
)
VALUES
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01', 'dddddddd-dddd-dddd-dddd-dddddddddd01', 'Employment contracts', 'Termination', 'Article 1', 'Termination notice', 'An employment relationship should not be terminated without respecting the notice period, written reasons, and any procedure required by the applicable employment contract and labour law. A worker should be informed of the reason for termination and should receive benefits owed under the law and contract.', 'English', 'verified', 1),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02', 'dddddddd-dddd-dddd-dddd-dddddddddd01', 'Working conditions', 'Leave', 'Article 2', 'Annual leave', 'A worker is entitled to annual leave under the applicable labour rules. Employers should keep records of leave, communicate leave arrangements clearly, and avoid denying statutory leave without a lawful reason.', 'English', 'verified', 2),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03', 'dddddddd-dddd-dddd-dddd-dddddddddd02', 'Company formation', 'Registration', 'Article 1', 'Company registration duties', 'A company should be registered with the competent registrar before operating as a company. Founders should provide required identification, company name, registered office, share information where applicable, and other documents required by the registrar.', 'English', 'active', 1),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee04', 'dddddddd-dddd-dddd-dddd-dddddddddd03', 'Processing principles', 'Consent', 'Article 1', 'Consent and lawful processing', 'Personal data should be processed on a lawful basis. Where consent is used, the consent should be specific, informed, and freely given. Data subjects should be told why their data is collected and how it will be used.', 'English', 'amended', 1)
ON CONFLICT (id) DO NOTHING;
