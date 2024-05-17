DO $$

DECLARE

  l_asset_id UUID;
  l_appointment_id UUID;

  wellbore_appointment RECORD;

BEGIN

  FOR wellbore_appointment IN (
    SELECT
      mwa.migratable_appointment_id
    , mwa.wellbore_id::text
    , mwa.wellbore_registration_number
    , mwa.appointed_operator_id
    , mwa.appointed_operator_name
    , mwa.responsible_from_date::date
    , mwa.responsible_to_date::date
    , mwa.is_exploration_phase::bool
    , mwa.is_development_phase::bool
    , mwa.is_decommissioning_phase::bool
    , mwa.appointment_source
    , mwa.legacy_nomination_reference
    FROM osd_migration.migratable_wellbore_appointments mwa
    ORDER BY mwa.migratable_appointment_id
  )
  LOOP

    -- is this portal asset already in the asset table?
    SELECT a.id
    INTO l_asset_id
    FROM osd.assets a
    WHERE a.portal_asset_id = wellbore_appointment.wellbore_id
    AND a.portal_asset_type = 'WELLBORE';

    -- if we don't have a wios asset id insert a new one
    IF l_asset_id IS NULL THEN

      l_asset_id := gen_random_uuid();

      INSERT INTO osd.assets(
        id
      , portal_asset_id
      , portal_asset_type
      , asset_name
      , status
      )
      VALUES (
        l_asset_id
      , wellbore_appointment.wellbore_id
      , 'WELLBORE'
      , wellbore_appointment.wellbore_registration_number
      , 'EXTANT'
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
    , status
    )
    VALUES(
      l_appointment_id
    , l_asset_id
    , wellbore_appointment.appointed_operator_id
    , wellbore_appointment.responsible_from_date
    , wellbore_appointment.responsible_to_date
    , wellbore_appointment.appointment_source
    , wellbore_appointment.legacy_nomination_reference
    , wellbore_appointment.responsible_from_date::timestamp
    , 'EXTANT'
    );

    -- create phase records
    IF wellbore_appointment.is_exploration_phase THEN

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
      , 'EXPLORATION_AND_APPRAISAL'
      );

    END IF;

    IF wellbore_appointment.is_development_phase THEN

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
      , 'DEVELOPMENT'
     );

    END IF;

    IF wellbore_appointment.is_decommissioning_phase THEN

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