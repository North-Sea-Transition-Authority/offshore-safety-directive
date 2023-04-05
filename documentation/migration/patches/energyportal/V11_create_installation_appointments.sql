DO $$

DECLARE

  l_asset_id UUID;
  l_appointment_id UUID;

  installation_appointment RECORD;

BEGIN

  FOR installation_appointment IN (
    SELECT
      mia.migratable_appointment_id
    , mia.installation_id::text
    , mia.installation_name
    , mia.appointed_operator_id
    , mia.appointed_operator_name
    , mia.responsible_from_date
    , mia.responsible_to_date
    , mia.is_development_phase::bool
    , mia.is_decommissioning_phase::bool
    , mia.appointment_source
    , mia.legacy_nomination_reference
    FROM osd_migration.migratable_installation_appointments mia
    ORDER BY mia.migratable_appointment_id
  )
  LOOP

    -- is this portal asset already in the asset table?
    SELECT a.id
    INTO l_asset_id
    FROM osd.assets a
    WHERE a.portal_asset_id = installation_appointment.installation_id
    AND a.portal_asset_type = 'INSTALLATION';

    -- if we don't have a wios asset id insert a new one
    IF l_asset_id IS NULL THEN

      l_asset_id := gen_random_uuid();

      INSERT INTO osd.assets(
        id
      , portal_asset_id
      , portal_asset_type
      , asset_name
      )
      VALUES (
        l_asset_id
      , installation_appointment.installation_id
      , 'INSTALLATION'
      , installation_appointment.installation_name
      );

    END IF;

    -- create an appointment record
    l_appointment_id := gen_random_uuid();

    INSERT INTO osd.appointments(
      id
    , asset_id
    , appointed_portal_operator_id
    , responsible_from_date
    , responsible_to_date
    , type
    , created_by_legacy_nomination_reference
    , created_datetime
    )
    VALUES(
      l_appointment_id
    , l_asset_id
    , installation_appointment.appointed_operator_id
    , installation_appointment.responsible_from_date
    , installation_appointment.responsible_to_date
    , installation_appointment.appointment_source
    , installation_appointment.legacy_nomination_reference
    , installation_appointment.responsible_from_date::timestamp
    );

    -- create phase records
    IF installation_appointment.is_development_phase THEN

      INSERT INTO osd.asset_phases(
        id
      , asset_id
      , appointment_id
      , phase
      )
      SELECT
        gen_random_uuid()
      , l_asset_id
      , l_appointment_id
      , phase
      FROM unnest(
        string_to_array(
          'DEVELOPMENT_DESIGN,DEVELOPMENT_CONSTRUCTION,DEVELOPMENT_INSTALLATION,DEVELOPMENT_COMMISSIONING,DEVELOPMENT_PRODUCTION'
        , ','
        )
      ) phase;

    END IF;

    IF installation_appointment.is_decommissioning_phase THEN

      INSERT INTO osd.asset_phases(
        id
      , asset_id
      , appointment_id
      , phase
      )
      VALUES (
        gen_random_uuid()
      , l_asset_id
      , l_appointment_id
      , 'DECOMMISSIONING'
      );

    END IF;

    COMMIT;

  END LOOP;

END;

$$;