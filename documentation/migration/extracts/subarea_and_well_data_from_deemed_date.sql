/* 1. CREATE SUBAREA SHAPES FOR ALL SUBAREAS EXISTING AT 19 JULY 2015 TO PRESENT DATE */

-- 1A. TABLE SETUP

DROP TABLE xviewmgr.dh_wios_subareas_since_2015
/

CREATE TABLE xviewmgr.dh_wios_subareas_since_2015 (
  licence_type VARCHAR2(4000) NOT NULL
, licence_no VARCHAR2(4000) NOT NULL
, block_ref VARCHAR2(4000) NOT NULL
, subarea_title VARCHAR2(4000) NOT NULL
, short_name VARCHAR2(4000) NOT NULL
, subarea_start_datetime DATE NOT NULL
, subarea_end_datetime DATE
, subarea_sid_area_id NUMBER NOT NULL
, subarea_feature_offset_high_m NUMBER NOT NULL
, subarea_feature_offset_low_m NUMBER NOT NULL
, null_out_srs NUMBER
, ed50_subarea_geometry mdsys.sdo_geometry
, etrs89_subarea_geometry mdsys.sdo_geometry
, id NUMBER GENERATED ALWAYS AS IDENTITY
) TABLESPACE tbsdata
/

CREATE UNIQUE INDEX dh_wios_subareas_since_2015_uk1 ON xviewmgr.dh_wios_subareas_since_2015 (block_ref, subarea_sid_area_id, subarea_start_datetime, subarea_end_datetime)
TABLESPACE tbsidx
/

BEGIN
  INSERT INTO mdsys.user_sdo_geom_metadata (
    TABLE_NAME
  , COLUMN_NAME
  , DIMINFO
  , SRID
  )
  VALUES (
    'DH_WIOS_SUBAREAS_SINCE_2015'
  , 'ED50_SUBAREA_GEOMETRY'
  , SDO_DIM_ARRAY(
      SDO_DIM_ELEMENT('X', -180, 180, 0.05)
    , SDO_DIM_ELEMENT('Y', -90, 90, 0.05)
    )
  , 4230  -- ED 50
  );
  COMMIT;
END;
/

CREATE INDEX xviewmgr.dh_wios_subareas_since_2015_sidx1 ON xviewmgr.dh_wios_subareas_since_2015 (ed50_subarea_geometry)
INDEXTYPE IS MDSYS.SPATIAL_INDEX
PARAMETERS ('TABLESPACE=TBSGEOIDX')
/

BEGIN
  INSERT INTO mdsys.user_sdo_geom_metadata (
    TABLE_NAME
  , COLUMN_NAME
  , DIMINFO
  , SRID
  )
  VALUES (
    'DH_WIOS_SUBAREAS_SINCE_2015'
  , 'ETRS89_SUBAREA_GEOMETRY'
  , SDO_DIM_ARRAY(
      SDO_DIM_ELEMENT('X', -180, 180, 0.05)
    , SDO_DIM_ELEMENT('Y', -90, 90, 0.05)
    )
  , 4258  -- ETRS89
  );
  COMMIT;
END;
/

CREATE INDEX xviewmgr.dh_wios_subareas_since_2015_sidx2 ON xviewmgr.dh_wios_subareas_since_2015 (etrs89_subarea_geometry)
INDEXTYPE IS MDSYS.SPATIAL_INDEX
PARAMETERS ('TABLESPACE=TBSGEOIDX')
/

-- 1B. INSERT SUBAREA DATA

INSERT INTO xviewmgr.dh_wios_subareas_since_2015 (
  licence_type
, licence_no
, block_ref
, subarea_title
, short_name
, subarea_start_datetime
, subarea_end_datetime
, subarea_sid_area_id
, subarea_feature_offset_high_m
, subarea_feature_offset_low_m
)
-- distinct here as some instances where same subarea info is duplicated on current data point
-- looks to be when subarea fields are added the start/end/shape/block_ref are the same but get a new subarea row
-- as all info we care about is identical use distinct to remove the duplicate
SELECT DISTINCT
  pcdp.licence_type
, pcdp.licence_no
, plbr.block_ref
, psa.title
, psa.short_name
-- use shape start date since subarea start refers to when subarea came into existence regardless of shape changing over time
, sip.period_start_datetime
-- use shape end date since not valid beyond that date
-- in some cases shape end not set even though should be, fallback to subarea end in these cases
, COALESCE(sip.period_end_datetime, psa.end_datetime) period_end_datetime
, sid_a.id subarea_sid_area_id
, COALESCE(sid_f.feature_offset_high_m, sid_f.feature_offset_max_m) feature_offset_high_m
, COALESCE(sid_f.feature_offset_low_m, sid_f.feature_offset_min_m) feature_offset_low_m
-- current data point contains full history of subarea changes over time
FROM pedmgr.ped_current_data_points pcdp
JOIN pedmgr.ped_subareas psa ON psa.ped_dp_id = pcdp.id
JOIN pedmgr.ped_licence_block_refs plbr ON plbr.plb_id = psa.ped_lb_id
JOIN spatialmgr.spatial_instance_periods sip ON sip.si_id = psa.si_id AND sip.status_control = 'C'
-- create separate rows for each area as need feature level offsets for later comparison against well depth
JOIN spatialmgr.spatial_instance_details sid_a ON sid_a.siv_id = sip.siv_id AND sid_a.class = 'A'
JOIN spatialmgr.spatial_instance_details sid_f ON sid_f.id = sid_a.ancestor_f_sid_id
WHERE pcdp.ped_sim_id = 0
AND pcdp.licence_type = 'P'
-- subareas that were active on or since deemed date
AND (sip.period_start_datetime >= '19-JUL-2015' OR COALESCE(sip.period_end_datetime, psa.end_datetime, SYSDATE) >= '19-JUL-2015')
-- some relinquished subareas will have NULL end date if no subarea end operation, hence only look at extant subareas
AND psa.status = 'EXTANT'
ORDER BY
  pcdp.licence_no
, plbr.block_ref
, psa.title
, psa.short_name
, sip.period_start_datetime
, COALESCE(sip.period_end_datetime, psa.end_datetime)
/

COMMIT
/

-- 1C. CREATE AREA GEOMETRY FOR SHAPE COORD SYSTEM USED (ED 50 OR ETRS 89)

DECLARE

  l_area_geometry mdsys.sdo_geometry;
  l_null_out_srs NUMBER;

BEGIN

  FOR subarea_rec IN (
    SELECT
      s.subarea_sid_area_id
    , sid.si_id
    , sid.class_srs
    , spatialmgr.spm.get_operation_parameter (
        p_layer_type         => 'SUBAREAS'
      , p_profile_usage_mnem => 'LICENSING_COMPARISONS'
      , p_srs_name           => sid.class_srs
      , p_param_name         => CASE spatialmgr.sp_datum.lookup_coord_type(sid.class_srs)
          WHEN 'GEOG' THEN 'GEOGRAPHIC_TOLERANCE'
          WHEN 'GRID' THEN 'CARTESIAN_TOLERANCE'
        END
      ) tolerance
    , spatialmgr.sp_datum.lookup_oracle_sdo_srid(sid.class_srs) using_target_srid
    , UPPER(spatialmgr.spm.get_operation_parameter ('SUBAREAS', 'LICENSING_COMPARISONS', sid.class_srs, 'DENSIFY_LOXODROMES')) densify_loxodromes
    , spatialmgr.spm.get_operation_parameter ('SUBAREAS', 'LICENSING_COMPARISONS', sid.class_srs, 'LON_OR_X_DENSIFICATION_RESOLUTION') lon_or_x_densification_res
    , spatialmgr.spm.get_operation_parameter ('SUBAREAS', 'LICENSING_COMPARISONS', sid.class_srs, 'LAT_OR_Y_DENSIFICATION_RESOLUTION') lat_or_y_densification_res
    , spatialmgr.spm.get_operation_parameter ('SUBAREAS', 'LICENSING_COMPARISONS', sid.class_srs, 'GRID_DENSIFICATION_TOLERANCE') grid_densification_tolerance
    FROM xviewmgr.dh_wios_subareas_since_2015 s
    JOIN spatialmgr.spatial_instance_details sid ON sid.id = s.subarea_sid_area_id
    WHERE (s.ed50_subarea_geometry IS NULL OR s.etrs89_subarea_geometry IS NULL)
  ) LOOP

    IF spatialmgr.spm.check_same_nav_type(p_si_list => bpmmgr.number_list_type(subarea_rec.si_id)) THEN

      l_area_geometry := spatialmgr.sp_command.get_area_geometry (
        p_area_sid_id => subarea_rec.subarea_sid_area_id
      , p_transformation_target_srid => subarea_rec.using_target_srid
      , p_tolerance => subarea_rec.tolerance
      , p_densify_loxodromes => 0
      , p_grid_densify_tolerance => 0
      );

      -- working in cartesian system so no need for srs, will null out for well and subarea later when doing checks
      l_null_out_srs := 1;

    ELSE

      l_area_geometry := spatialmgr.sp_command.get_area_geometry (
        p_area_sid_id => subarea_rec.subarea_sid_area_id
      , p_transformation_target_srid => subarea_rec.using_target_srid
      , p_tolerance => subarea_rec.tolerance
      , p_densify_loxodromes => CASE WHEN subarea_rec.densify_loxodromes = 'TRUE' THEN 1 ELSE 0 END
      , p_x_densify_res => subarea_rec.lon_or_x_densification_res
      , p_y_densify_res => subarea_rec.lat_or_y_densification_res
      , p_grid_densify_tolerance => subarea_rec.grid_densification_tolerance
      );

      l_null_out_srs := 0;

    END IF;

    IF subarea_rec.class_srs = 'ED 50' THEN

      UPDATE xviewmgr.dh_wios_subareas_since_2015
      SET ed50_subarea_geometry = l_area_geometry
      , null_out_srs = l_null_out_srs
      WHERE subarea_sid_area_id = subarea_rec.subarea_sid_area_id;

    ELSIF subarea_rec.class_srs = 'ETRS89' THEN

      UPDATE xviewmgr.dh_wios_subareas_since_2015
      SET etrs89_subarea_geometry = l_area_geometry
      , null_out_srs = l_null_out_srs
      WHERE subarea_sid_area_id = subarea_rec.subarea_sid_area_id;

    ELSE

      RAISE_APPLICATION_ERROR(-20000, 'Unexpected class srs ' || subarea_rec.class_srs);

    END IF;

    COMMIT;

  END LOOP;

END;
/

-- 1D. CHECK ALL SUBAREAS HAD SHAPES CREATED
-- SHOULD RETURN 0 ROWS
SELECT *
FROM xviewmgr.dh_wios_subareas_since_2015 s
WHERE s.ed50_subarea_geometry IS NULL
AND s.etrs89_subarea_geometry IS NULL
/

/* 2. CREATE DATASET OF WELLBORES TO COMPARE AGAINST */

-- 2A. CREATE DATA TABLE

DROP TABLE xviewmgr.dh_wios_well_data
/

CREATE TABLE xviewmgr.dh_wios_well_data (
  wellbore_id NUMBER
, wellbore_detail_id NUMBER
, well_registration_no VARCHAR2(4000)
, spud_date DATE
, operators_wellbore_name VARCHAR2(4000)
, well_mechanical_status VARCHAR2(4000)
, operational_status_mnem VARCHAR2(4000)
, wons_subarea_operator VARCHAR2(4000)
, wons_competent_operator VARCHAR2(4000)
, wons_origin_licence VARCHAR2(4000)
, wons_origin_quadrant_no VARCHAR2(4000)
, wons_origin_block_no VARCHAR2(4000)
, wons_origin_canonical VARCHAR2(4000)
, wons_td_licence VARCHAR2(4000)
, wons_td_canonical VARCHAR2(4000)
, wons_td_depth_m NUMBER
) TABLESPACE tbsdata
/

CREATE UNIQUE INDEX dh_wios_well_data_uk1 ON xviewmgr.dh_wios_well_data (wellbore_id)
TABLESPACE tbsidx
/

-- 2B. ADD KEY INFO WE NEED FROM ALL EXTANT WELLBORES

INSERT INTO xviewmgr.dh_wios_well_data (
  wellbore_id
, wellbore_detail_id
, well_registration_no
, spud_date
, operators_wellbore_name
, well_mechanical_status
, operational_status_mnem
, wons_subarea_operator
, wons_competent_operator
, wons_origin_licence
, wons_origin_quadrant_no
, wons_origin_block_no
, wons_td_licence
, wons_td_depth_m
)
SELECT
  xwws.w_id wellbore_id
, xwws.wd_id wellbore_detail_id
, xwws.well_registration_no
, xwws.spud_date
, xwws.operators_wellbore_name
, wms.display_name well_mechanical_status
, wms.operational_status_mnem
, decmgr.organisation.get_name(xwws.subarea_operator_ou_id) wons_subarea_operator
, decmgr.organisation.get_name(xwws.competent_operator_ou_id) wons_competent_operator
, wo.licence_type||wo.licence_no wons_origin_licence
, wo.quadrant_no wons_quadrant_no
, wo.block_no wons_block_no
, plm.licence_type||plm.licence_no wons_td_licence
-- convert to common unit of measure (metres) for comparison
-- all depth is subsea so convert to negative values for comparison
, CASE xwws.td_tvdss_unit
    WHEN 'FEET'
    THEN xwws.td_tvdss * 0.3048
    WHEN 'METRES'
    THEN xwws.td_tvdss
    ELSE NULL
  END * -1 total_depth_m2
FROM wellmgr.extant_wellbores_current ewc
JOIN wellmgr.xview_wons_wellbore_search xwws ON xwws.wd_id = ewc.wd_id
JOIN wellmgr.xview_wons_well_origin_search wo ON wo.wod_id = ewc.wod_id
LEFT JOIN wellmgr.wellbore_mechanical_statuses wms ON xwws.wellbore_mechanical_status = wms.mnem
LEFT JOIN pedmgr.ped_licence_master plm ON plm.id = xwws.td_licence_plm_id
WHERE wo.regulatory_jurisdiction = 'SEAWARD'
/

COMMIT
/

-- 2C. AUGMENT WITH CANONICAL INFORMATION FOR ORIGIN AND TD

DECLARE

  l_error_msg VARCHAR2(4000);
  l_canonical VARCHAR2(4000);

  FUNCTION get_canonical(
    p_datum IN VARCHAR
  , p_coordinate_type IN VARCHAR
  , p_lat_decimal_degrees IN NUMBER
  , p_lon_decimal_degrees IN NUMBER
  , p_lat_degrees IN NUMBER
  , p_lat_minutes IN NUMBER
  , p_lat_seconds IN NUMBER
  , p_lat_north_south IN VARCHAR2
  , p_lon_degrees IN NUMBER
  , p_lon_minutes IN NUMBER
  , p_lon_seconds IN NUMBER
  , p_lon_east_west IN VARCHAR2
  , p_ng_northing VARCHAR2
  , p_ng_easting VARCHAR2
  , p_ng_reference VARCHAR2
  ) RETURN VARCHAR2
  IS

    l_canonical VARCHAR2(4000);
    l_error_message VARCHAR2(4000);

  BEGIN

    CASE

      WHEN p_coordinate_type IS NULL
        THEN l_canonical := NULL;

      WHEN p_coordinate_type = 'DECIMAL_DEGREES'
        THEN
          l_canonical := wellmgr.wons_spatial.convert_dec_degs_to_canonical(
            p_datum
          , p_lat_decimal_degrees
          , p_lon_decimal_degrees
          , l_error_message
          );

      WHEN p_coordinate_type = 'DEGREES_MINUTES_SECONDS'
        THEN
          l_canonical := wellmgr.wons_spatial.convert_lat_long_to_canonical(
            p_datum
          , p_lat_degrees
          , p_lat_minutes
          , p_lat_seconds
          , p_lat_north_south
          , p_lon_degrees
          , p_lon_minutes
          , p_lon_seconds
          , p_lon_east_west
          , l_error_message
          );

      WHEN p_coordinate_type = 'NATIONAL_GRID_EASTING_NORTHING'
        THEN
          l_canonical := wellmgr.wons_spatial.convert_ng_en_to_canonical(
            p_datum
          , p_ng_easting
          , p_ng_northing
          , l_error_message
          );

      WHEN p_coordinate_type = 'NATIONAL_GRID_REF'
        THEN
          l_canonical := wellmgr.wons_spatial.convert_ng_ref_to_canonical(
            p_datum
          , p_ng_reference
          , l_error_message
          );

    END CASE;

    RETURN l_canonical;

  END get_canonical;

BEGIN

  FOR well_rec IN (
    -- well origin coord info
    SELECT
      w.wellbore_detail_id
    , 'ORIGIN' source
    , wo.coord_type
    , wo.datum
    , wo.lat_decimal_degrees
    , wo.long_decimal_degrees
    , wo.lat_degrees
    , wo.lat_minutes
    , wo.lat_seconds
    , wo.lat_north_south
    , wo.long_degrees
    , wo.long_minutes
    , wo.long_seconds
    , wo.long_east_west
    , wo.ng_reference
    , wo.ng_easting
    , wo.ng_northing
    FROM xviewmgr.dh_wios_well_data w
    JOIN wellmgr.extant_wellbores_current ewc ON ewc.wd_id = w.wellbore_detail_id
    JOIN wellmgr.xview_wons_well_origin_search wo ON wo.wod_id = ewc.wod_id
    -- td coord info
    UNION
    SELECT
      w.wellbore_detail_id
    , 'TD' source
    , xwws.coord_type
    , xwws.datum
    , xwws.lat_decimal_degrees
    , xwws.long_decimal_degrees
    , xwws.lat_degrees
    , xwws.lat_minutes
    , xwws.lat_seconds
    , xwws.lat_north_south
    , xwws.long_degrees
    , xwws.long_minutes
    , xwws.long_seconds
    , xwws.long_east_west
    , xwws.ng_reference
    , xwws.ng_easting
    , xwws.ng_northing
    FROM xviewmgr.dh_wios_well_data w
    JOIN wellmgr.extant_wellbores_current ewc ON ewc.wd_id = w.wellbore_detail_id
    JOIN wellmgr.xview_wons_wellbore_search xwws ON xwws.wd_id = ewc.wd_id
  )
  LOOP

    l_canonical := get_canonical(
      p_datum => well_rec.datum
    , p_coordinate_type => well_rec.coord_type
    , p_lat_decimal_degrees => well_rec.lat_decimal_degrees
    , p_lon_decimal_degrees => well_rec.long_decimal_degrees
    , p_lat_degrees => well_rec.lat_degrees
    , p_lat_minutes => well_rec.lat_minutes
    , p_lat_seconds => well_rec.lat_seconds
    , p_lat_north_south => well_rec.lat_north_south
    , p_lon_degrees => well_rec.long_degrees
    , p_lon_minutes => well_rec.long_minutes
    , p_lon_seconds => well_rec.long_seconds
    , p_lon_east_west => well_rec.long_east_west
    , p_ng_northing => well_rec.ng_northing
    , p_ng_easting => well_rec.ng_easting
    , p_ng_reference => well_rec.ng_reference
    );

    IF well_rec.source = 'ORIGIN' THEN

      UPDATE xviewmgr.dh_wios_well_data w
      SET w.wons_origin_canonical = l_canonical
      WHERE w.wellbore_detail_id = well_rec.wellbore_detail_id;

    ELSE

      UPDATE xviewmgr.dh_wios_well_data w
      SET w.wons_td_canonical = l_canonical
      WHERE w.wellbore_detail_id = well_rec.wellbore_detail_id;

    END IF;

  END LOOP;

END;
/

COMMIT
/