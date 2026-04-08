INSERT INTO osd.nominations (id, created_datetime, reference)
VALUES
  ('d5aff607-b597-48db-9034-f464d896467c', '2026-03-25 16:20:20.215649 +00:00', 'WIO/2026/1'),
  ('07a9585d-3e1a-4e39-8f34-6754ffa80fe9', '2026-03-26 11:24:53.241896 +00:00', 'WIO/2026/2');

INSERT INTO osd.nomination_details (id, nomination_id, created_datetime, version, status, submitted_datetime)
VALUES
  ('3fc81af9-7611-4f22-ba1d-e990c19b279c', 'd5aff607-b597-48db-9034-f464d896467c', '2026-03-25 16:20:20.215649 +00:00', 1, 'SUBMITTED', '2026-03-25 16:22:10.148472 +00:00'),
  ('0b5a9f87-5a77-466b-9ea8-bf8929c80e18', 'd5aff607-b597-48db-9034-f464d896467c', '2026-03-25 17:37:34.526913 +00:00', 2, 'WITHDRAWN', '2026-03-25 17:37:46.149100 +00:00'),
  ('3524b7eb-4ba7-41be-bdd9-6280865ed1d0', '07a9585d-3e1a-4e39-8f34-6754ffa80fe9', '2026-03-26 11:24:53.241896 +00:00', 1, 'SUBMITTED', '2026-03-26 11:26:16.635448 +00:00'),
  ('0cb4d068-fbfb-4217-b0ee-e969f9f66ab4', '07a9585d-3e1a-4e39-8f34-6754ffa80fe9', '2026-03-27 11:24:53.241896 +00:00', null, 'DELETED', '2026-03-27 11:26:16.635448 +00:00');

INSERT INTO osd.applicant_details (id, nomination_detail_id, portal_organisation_id, applicant_reference)
VALUES
  ('57abc771-500a-4649-964f-323481112d8f', '3fc81af9-7611-4f22-ba1d-e990c19b279c', 12, null),
  ('fb4662b2-8c0b-45ff-9bfa-230fe86d9ae5', '0b5a9f87-5a77-466b-9ea8-bf8929c80e18', 12, null),
  ('a4d253a7-629d-48da-953a-bddeb70f9824', '3524b7eb-4ba7-41be-bdd9-6280865ed1d0', 1307, null);

INSERT INTO osd.case_events (uuid, type, nomination_id, nomination_version, created_by, event_timestamp, created_timestamp, comment, title)
VALUES
  ('e67fc6eb-f1f1-48ed-90b0-5339d191f139', 'NOMINATION_SUBMITTED', 'd5aff607-b597-48db-9034-f464d896467c', 1, 53641, '2026-03-25 16:22:10.148472 +00:00', '2026-03-25 16:22:10.172836 +00:00', null, null),
  ('880bc465-9a51-4c44-98c2-f6212afd3673', 'UPDATE_REQUESTED', 'd5aff607-b597-48db-9034-f464d896467c', 1, 53173, '2026-03-25 16:23:13.603192 +00:00', '2026-03-25 16:23:13.603192 +00:00', 'update requested', null),
  ('d4fcd0d9-06db-42ca-be00-c8ac6c5c44ce', 'NOMINATION_SUBMITTED', 'd5aff607-b597-48db-9034-f464d896467c', 2, 53641, '2026-03-25 17:37:46.149100 +00:00', '2026-03-25 17:37:46.153674 +00:00', null, null),
  ('7fed8e32-91d8-417a-8899-195d323e8d3d', 'UPDATE_REQUESTED', 'd5aff607-b597-48db-9034-f464d896467c', 2, 53173, '2026-03-26 11:01:16.422593 +00:00', '2026-03-26 11:01:16.422593 +00:00', 'update requested', null),
  ('fbad4a3a-aa09-43f7-9452-e61107a7b750', 'WITHDRAWN', 'd5aff607-b597-48db-9034-f464d896467c', 2, 53173, '2026-03-26 11:02:06.492288 +00:00', '2026-03-26 11:02:06.492288 +00:00', 'withdrawn', null),
  ('ede8d083-7969-4567-8547-1bbff5a2d2f0', 'NOMINATION_SUBMITTED', '07a9585d-3e1a-4e39-8f34-6754ffa80fe9', 1, 53641, '2026-03-26 11:26:16.635448 +00:00', '2026-03-26 11:26:16.653434 +00:00', null, null),
  ('aebfd833-7200-483b-9161-d05878e4d309', 'UPDATE_REQUESTED', '07a9585d-3e1a-4e39-8f34-6754ffa80fe9', 1, 53173, '2026-03-26 11:41:04.245507 +00:00', '2026-03-26 11:41:04.245507 +00:00', 'update requested', null);
