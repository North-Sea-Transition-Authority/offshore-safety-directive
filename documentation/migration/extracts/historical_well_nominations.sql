/* 1. CREATE AND VALIDATE NSTA PROVIDED DATA */

-- 1A. TABLE SETUP

DROP TABLE xviewmgr.dh_wios_nsta_appointments
/

CREATE TABLE xviewmgr.dh_wios_nsta_appointments (
  nomination_reference VARCHAR2(4000)
  , appointment_operator_name VARCHAR2(4000)
  , appointment_operator_no VARCHAR2(4000)
  , licence VARCHAR2(4000)
  , quadrant VARCHAR2(4000)
  , block VARCHAR2(4000)
  , block_suffix VARCHAR2(4000)
  , subarea_long_name VARCHAR2(4000)
  , appointment_date DATE
  , appointment_id NUMBER GENERATED ALWAYS AS IDENTITY
) TABLESPACE tbsdata
/

-- 1B. IMPORT DATA


-- 1C. CLEAN IMPORTED DATA
UPDATE xviewmgr.dh_wios_nsta_appointments a
SET
  nomination_reference = TRIM(nomination_reference)
  , appointment_operator_name = UPPER(TRIM(appointment_operator_name))
  , appointment_operator_no = TRIM(appointment_operator_no)
  , licence = TRIM(REGEXP_REPLACE(licence,'[' ||CHR(10)||CHR(13)||']'))
  , quadrant = TRIM(quadrant)
  , block = TRIM(block)
  , block_suffix = TRIM(block_suffix)
  , subarea_long_name = TRIM(REGEXP_REPLACE(subarea_long_name,'[' ||CHR(10)||CHR(13)||']'))
/

COMMIT
/

-- 1C. VALIDATE LICENCE, BLOCK AND SUBAREA EXISTS ON APPOINTMENT DATE
-- SHOULD RETURN 0 ROWS
SELECT *
FROM xviewmgr.dh_wios_nsta_appointments a
WHERE NOT EXISTS (
  SELECT 1
  FROM xviewmgr.dh_wios_subareas_since_2015 s
  WHERE a.licence = s.licence_type||s.licence_no
      AND a.quadrant||'/'||a.block||a.block_suffix = s.block_ref
      AND a.subarea_long_name = s.subarea_title
      AND a.appointment_date >= s.subarea_start_datetime
        -- don't count subareas that ended on the appointment date since they are superseded by new subareas
      AND a.appointment_date < COALESCE(s.subarea_end_datetime, SYSDATE + 1)
)
ORDER BY
  a.nomination_reference
, a.licence
, a.quadrant
, a.block
, a.block_suffix
, a.subarea_long_name
/

-- 1D. VALIDATE THAT THE OPERATOR PROVIDED EXISTS
-- SHOULD RETURN 0 ROWS
SELECT *
FROM xviewmgr.dh_wios_nsta_appointments a
WHERE NOT EXISTS ( -- no match on reg number
  SELECT 1
  FROM decmgr.xview_organisation_units xou
  WHERE xou.registered_number = a.appointment_operator_no
)
    AND ( -- where we can't match on reg number above we can match uniquely by name
        SELECT COUNT(DISTINCT xon.organ_id)
        FROM decmgr.xview_organisation_names xon
        WHERE xon.name = a.appointment_operator_name
      ) != 1
ORDER BY
  a.appointment_operator_name
, a.licence
, a.quadrant
, a.block
, a.block_suffix
, a.subarea_long_name
/

/* 2. IDENTIFY WELLS IN SUBAREA APPOINTMENT IS FOR ON THE APPOINTMENT DATE */

-- 2A. TABLE SETUP FOR OUTPUT

DROP TABLE xviewmgr.dh_wios_appointment_subarea_wells
  /

CREATE TABLE xviewmgr.dh_wios_appointment_subarea_wells (
  appointment_id VARCHAR2(4000)
, wellbore_detail_id NUMBER
, td_or_origin_match VARCHAR2(4000)
, materialised_subarea_id NUMBER
) TABLESPACE tbsdata
/

-- 2B. IDENTIFY WELLS IN SUBAREA AT APPOINTMENT DATE
DECLARE

  l_ed50_srid NUMBER;
  l_ed50_tolerance NUMBER;
  l_ed50_cartesian_tolerance NUMBER;

  l_etrs89_srid NUMBER;
  l_etrs89_tolerance NUMBER;
  l_etrs89_cartesian_tolerance NUMBER;

  l_well_count NUMBER;
  l_cur_count NUMBER := 0;

  K_TD CONSTANT VARCHAR2(4000) := 'TD';
  K_ORIGIN CONSTANT VARCHAR2(4000) := 'ORIGIN';

  PROCEDURE add_well_to_subarea_appointments (
    p_wellbore_detail_id NUMBER
  , p_canonical VARCHAR2
  , p_origin_or_td VARCHAR2
  , p_spud_date DATE
  , p_well_td_depth_m NUMBER DEFAULT NULL
  )
  IS

    K_ORACLE_SPATIAL_RELATIONSHIPS CONSTANT VARCHAR2(4000) := 'INSIDE+COVERDBY+ON';

    l_base_well_geometry mdsys.sdo_geometry;
    l_ed50_well_geometry mdsys.sdo_geometry;
    l_etrs89_well_geometry mdsys.sdo_geometry;

  BEGIN

    l_base_well_geometry := spatialmgr.sp_coord.build_point_geometry(p_canonical);

    -- create ed50 well for checking against ed50 subareas
    l_ed50_well_geometry := sdo_cs.transform (
      l_base_well_geometry
    , l_ed50_srid
    );

    -- create etrs89 well for checking against etrs89 subareas
    l_etrs89_well_geometry := sdo_cs.transform (
      l_base_well_geometry
    , l_etrs89_srid
    );

    INSERT INTO xviewmgr.dh_wios_appointment_subarea_wells
    WITH target_subareas AS (
      SELECT
        a.appointment_id
           , s.null_out_srs
           -- determine which well geom we are using based on subarea srs
           , CASE
        WHEN s.ed50_subarea_geometry IS NOT NULL
          THEN l_ed50_well_geometry
        WHEN s.etrs89_subarea_geometry IS NOT NULL
          THEN l_etrs89_well_geometry
        ELSE NULL -- will never get this based on checks in data source
        END well_geometry
           -- mutually exclusive
           , COALESCE(s.ed50_subarea_geometry, s.etrs89_subarea_geometry) subarea_geometry
           -- get tolerance based on if removing srid and srs type
           , CASE
        WHEN s.null_out_srs = 0 AND s.ed50_subarea_geometry IS NOT NULL
          THEN l_ed50_tolerance
        WHEN s.null_out_srs = 1 AND s.ed50_subarea_geometry IS NOT NULL
          THEN l_ed50_cartesian_tolerance
        WHEN s.null_out_srs = 0 AND s.etrs89_subarea_geometry IS NOT NULL
          THEN l_etrs89_tolerance
        WHEN s.null_out_srs = 1 AND s.etrs89_subarea_geometry IS NOT NULL
          THEN l_etrs89_cartesian_tolerance
        ELSE NULL -- will never get this based on checks in data source
        END target_tolerance
           , s.id materialised_subarea_id
      FROM xviewmgr.dh_wios_nsta_appointments a
           JOIN xviewmgr.dh_wios_subareas_since_2015 s ON a.licence = s.licence_type||s.licence_no
        AND a.quadrant||'/'||a.block||a.block_suffix = s.block_ref
        AND a.subarea_long_name = s.subarea_title
        AND a.appointment_date >= s.subarea_start_datetime
        -- don't count subareas that ended on the appointment date since they are superseded by new subareas
        AND a.appointment_date < COALESCE(s.subarea_end_datetime, SYSDATE + 1)
           -- well only for appointment if already spudded by appointment date
      WHERE p_spud_date <= a.appointment_date
            -- when td then take into account td depth
          AND (p_well_td_depth_m IS NULL OR p_well_td_depth_m BETWEEN s.subarea_feature_offset_low_m AND s.subarea_feature_offset_high_m)
    )
    , relation_params AS (
      SELECT
        ts.appointment_id
           , CASE ts.null_out_srs
        WHEN 0
          THEN ts.well_geometry
        ELSE sdo_geometry(ts.well_geometry.sdo_gtype, NULL, ts.well_geometry.sdo_point, NULL, NULL)
        END formatted_well_geometry
           , CASE ts.null_out_srs
        WHEN 0
          THEN ts.subarea_geometry
        ELSE sdo_geometry(ts.subarea_geometry.sdo_gtype, NULL, NULL, ts.subarea_geometry.sdo_elem_info, ts.subarea_geometry.sdo_ordinates)
        END formatted_subarea_geometry
           , ts.target_tolerance
           , ts.materialised_subarea_id
      FROM target_subareas ts
    )
    SELECT
      rp.appointment_id
    , p_wellbore_detail_id
    , p_origin_or_td
    , rp.materialised_subarea_id
    FROM relation_params rp
    WHERE sdo_geom.relate(rp.formatted_well_geometry, K_ORACLE_SPATIAL_RELATIONSHIPS, rp.formatted_subarea_geometry, rp.target_tolerance) != 'FALSE';

  END add_well_to_subarea_appointments;

BEGIN

  l_ed50_srid := spatialmgr.sp_datum.lookup_oracle_sdo_srid('ED 50');
  l_ed50_tolerance := spatialmgr.spm.get_operation_parameter (
    p_layer_type         => 'SUBAREAS'
  , p_profile_usage_mnem => 'LICENSING_COMPARISONS'
  , p_srs_name           => 'ED 50'
  , p_param_name         => 'GEOGRAPHIC_TOLERANCE'
  );
  l_ed50_cartesian_tolerance := spatialmgr.spm.get_operation_parameter (
    p_layer_type         => 'SUBAREAS'
  , p_profile_usage_mnem => 'LICENSING_COMPARISONS'
  , p_srs_name           => 'ED 50'
  , p_param_name         => 'CARTESIAN_TOLERANCE'
  );

  l_etrs89_srid := spatialmgr.sp_datum.lookup_oracle_sdo_srid('ETRS89');
  l_etrs89_tolerance := spatialmgr.spm.get_operation_parameter (
    p_layer_type         => 'SUBAREAS'
  , p_profile_usage_mnem => 'LICENSING_COMPARISONS'
  , p_srs_name           => 'ETRS89'
  , p_param_name         => 'GEOGRAPHIC_TOLERANCE'
  );
  l_etrs89_cartesian_tolerance := spatialmgr.spm.get_operation_parameter (
    p_layer_type         => 'SUBAREAS'
  , p_profile_usage_mnem => 'LICENSING_COMPARISONS'
  , p_srs_name           => 'ETRS89'
  , p_param_name         => 'CARTESIAN_TOLERANCE'
  );

SELECT COUNT(*)
INTO l_well_count
FROM xviewmgr.dh_wios_well_data w
-- don't rerun where we have already checked a well
WHERE NOT EXISTS (
  SELECT 1
  FROM xviewmgr.dh_wios_appointment_subarea_wells wasw
  WHERE wasw.wellbore_detail_id = w.wellbore_detail_id
);

FOR well_rec IN (
  SELECT
    w.wellbore_detail_id
  , w.spud_date
  , w.wons_origin_canonical
  , w.wons_td_canonical
  , w.wons_td_depth_m
  FROM xviewmgr.dh_wios_well_data w
  -- don't rerun where we have already checked a well
  WHERE NOT EXISTS (
    SELECT 1
    FROM xviewmgr.dh_wios_appointment_subarea_wells wasw
    WHERE wasw.wellbore_detail_id = w.wellbore_detail_id
  )
)
LOOP

  l_cur_count := l_cur_count + 1;
  dbms_application_info.set_client_info('Processing well ' || l_cur_count || ' of ' || l_well_count);

  IF well_rec.wons_origin_canonical IS NOT NULL THEN

    add_well_to_subarea_appointments (
      p_wellbore_detail_id => well_rec.wellbore_detail_id
    , p_canonical => well_rec.wons_origin_canonical
    , p_origin_or_td => K_ORIGIN
    , p_spud_date => well_rec.spud_date
    );

  END IF;

  IF well_rec.wons_td_canonical IS NOT NULL THEN

    add_well_to_subarea_appointments (
      p_wellbore_detail_id => well_rec.wellbore_detail_id
    , p_canonical => well_rec.wons_td_canonical
    , p_origin_or_td => K_TD
    , p_spud_date => well_rec.spud_date
    , p_well_td_depth_m => well_rec.wons_td_depth_m
    );

END IF;

-- commit to avoid holding lots in memory
COMMIT;

END LOOP;

END;
/


-- 2C. EXTRACT DATA FOR NSTA
WITH extract_data AS (
  SELECT
    a.nomination_reference
  , a.appointment_operator_name
  , a.appointment_operator_no
  , a.licence
  , a.quadrant
  , a.block
  , a.block_suffix
  , a.subarea_long_name
  , a.appointment_date
  , st.join(stagg(s.subarea_feature_offset_low_m || ' to ' || s.subarea_feature_offset_high_m), ', ', NULL, NULL, NULL, 'true') in_strata
  , w.well_registration_no
  , w.spud_date
  , w.operational_status_mnem
  , w.well_mechanical_status
  , xwws.original_wellbore_intent
  , xwws.wellbore_intent
  , st.join(stagg(ab3.ab3_date), ', ', NULL, NULL, NULL, 'true') ab3_dates
  , w.wons_origin_licence
  , w.wons_td_licence
  , w.wons_td_depth_m
  , st.join(stagg(sw.td_or_origin_match), ', ', NULL, NULL, NULL, 'true') match_origin_or_td
  FROM xviewmgr.dh_wios_nsta_appointments a
  LEFT JOIN xviewmgr.dh_wios_appointment_subarea_wells sw ON sw.appointment_id = a.appointment_id
  LEFT JOIN xviewmgr.dh_wios_subareas_since_2015 s ON s.id = sw.materialised_subarea_id
  LEFT JOIN xviewmgr.dh_wios_well_data w ON w.wellbore_detail_id = sw.wellbore_detail_id
  LEFT JOIN wellmgr.xview_wons_wellbore_search xwws ON xwws.wd_id = w.wellbore_detail_id
  LEFT JOIN wellmgr.vw_ab3_notifications ab3 ON ab3.w_id = xwws.w_id
  GROUP BY
    a.nomination_reference
  , a.appointment_operator_name
  , a.appointment_operator_no
  , a.licence
  , a.quadrant
  , a.block
  , a.block_suffix
  , a.subarea_long_name
  , a.appointment_date
  , w.well_registration_no
  , w.spud_date
  , w.operational_status_mnem
  , w.well_mechanical_status
  , xwws.original_wellbore_intent
  , xwws.wellbore_intent
  , w.wons_origin_licence
  , w.wons_td_licence
  , w.wons_td_depth_m
)
SELECT
  ed.*
, CASE
    WHEN ed.well_registration_no IS NULL
      THEN NULL
    WHEN (ed.match_origin_or_td = 'ORIGIN' AND ed.licence = ed.wons_origin_licence) OR (ed.match_origin_or_td = 'TD' AND ed.licence = ed.wons_td_licence) OR (ed.licence = ed.wons_origin_licence AND ed.licence = ed.wons_td_licence) -- origin and td and licence matches
      THEN 'Same licence'
    WHEN (ed.match_origin_or_td = 'ORIGIN' AND ed.wons_origin_licence IS NULL) OR (ed.match_origin_or_td = 'TD' AND ed.wons_td_licence IS NULL) OR (ed.wons_origin_licence IS NULL AND ed.wons_td_licence IS NULL)
      THEN 'Missing licence'
    WHEN ed.match_origin_or_td LIKE '%ORIGIN%' AND ed.match_origin_or_td LIKE '%TD%' AND ed.licence = ed.wons_origin_licence AND ed.wons_td_licence IS NULL
      THEN 'Origin licence match, missing TD licence'
    WHEN ed.match_origin_or_td LIKE '%ORIGIN%' AND ed.match_origin_or_td LIKE '%TD%' AND ed.wons_origin_licence IS NULL AND ed.licence = ed.wons_td_licence
      THEN 'TD licence match, missing origin licence'
    ELSE 'WONS licence does not match'
  END licence_comparison
FROM extract_data ed
ORDER BY
  ed.nomination_reference
, ed.licence
, ed.quadrant
, ed.block
, ed.block_suffix
, ed.subarea_long_name
/