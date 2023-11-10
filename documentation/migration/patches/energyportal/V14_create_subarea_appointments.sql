DO $$

DECLARE

  l_asset_id UUID;
  l_appointment_id UUID;
  l_created_by_appointment_id UUID;

  subarea_appointment RECORD;

BEGIN

  FOR subarea_appointment IN (
    SELECT
      msa.migratable_appointment_id
    , msa.subarea_id
    , msa.subarea_reference
    , msa.appointed_operator_id
    , msa.appointed_operator_name
    , msa.responsible_from_date
    , msa.responsible_to_date
    , msa.is_exploration_phase::bool
    , msa.is_development_phase::bool
    , msa.is_decommissioning_phase::bool
    , msa.appointment_source
    , msa.legacy_nomination_reference
    , msa.created_by_migratable_appointment_id
    , msa.asset_status
    FROM osd_migration.migratable_subarea_appointments msa
    -- created_by_migratable_appointment_id nulls first so we make any appointments that
    -- will be referenced by other appointments first so we know they exist when we lookup
    ORDER BY msa.migratable_appointment_id, msa.created_by_migratable_appointment_id NULLS FIRST
  )
  LOOP

    -- is this portal asset already in the asset table?
    SELECT a.id
    INTO l_asset_id
    FROM osd.assets a
    WHERE a.portal_asset_id = subarea_appointment.subarea_id
    AND a.portal_asset_type = 'SUBAREA'
    AND a.status = subarea_appointment.asset_status;

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
      , subarea_appointment.subarea_id
      , 'SUBAREA'
      , subarea_appointment.subarea_reference
      , subarea_appointment.asset_status
      );

    END IF;

    -- create an appointment record
    l_appointment_id := gen_random_uuid();

    IF subarea_appointment.created_by_migratable_appointment_id IS NOT NULL THEN

      SELECT appointment_lookup.appointment_id
      INTO l_created_by_appointment_id
      FROM osd_migration.subarea_migration_appointment_lookup appointment_lookup
      WHERE appointment_lookup.migratable_appointment_id = subarea_appointment.created_by_migratable_appointment_id;

    END IF;

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
    , created_by_appointment_id
    )
    VALUES(
      l_appointment_id
    , l_asset_id
    , subarea_appointment.appointed_operator_id
    , subarea_appointment.responsible_from_date
    , subarea_appointment.responsible_to_date
    , subarea_appointment.appointment_source
    , subarea_appointment.legacy_nomination_reference
    , subarea_appointment.responsible_from_date::timestamp
    , 'EXTANT'
    , l_created_by_appointment_id
    );

    INSERT INTO osd_migration.subarea_migration_appointment_lookup(
      migratable_appointment_id
    , appointment_id
    ) VALUES (
      subarea_appointment.migratable_appointment_id
    , l_appointment_id
    );

    -- create phase records
    IF subarea_appointment.is_exploration_phase THEN

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

    IF subarea_appointment.is_development_phase THEN

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

    IF subarea_appointment.is_decommissioning_phase THEN

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