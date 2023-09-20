GRANT SELECT ON pedmgr.ped_licence_master TO wios_migration
/
GRANT SELECT ON pedmgr.ped_current_data_points TO wios_migration
/
GRANT SELECT ON pedmgr.ped_licence_blocks TO wios_migration
/
GRANT SELECT ON pedmgr.ped_licence_block_refs TO wios_migration
/
GRANT SELECT ON pedmgr.ped_subareas TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortiums TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortium_details TO wios_migration
/
GRANT SELECT ON decmgr.xview_organisation_units TO wios_migration
/
GRANT SELECT ON decmgr.current_org_grp_organisations TO wios_migration
/
GRANT SELECT ON decmgr.current_organisation_groups TO wios_migration
/
GRANT SELECT ON pedmgr.ped_licences TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortiums TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortium_details TO wios_migration
/
GRANT SELECT ON decmgr.xview_organisation_units TO wios_migration
/
GRANT SELECT ON pedmgr.epa_subareas TO wios_migration
/
GRANT SELECT ON wellmgr.extant_wellbores_current TO wios_migration
/
GRANT SELECT ON wellmgr.xview_wons_wellbore_search TO wios_migration
/
GRANT SELECT ON wellmgr.xview_wons_well_origin_search TO wios_migration
/
GRANT SELECT ON decmgr.xview_organisation_units TO wios_migration
/
GRANT SELECT ON wellmgr.wellbore_mechanical_statuses TO wios_migration
/
GRANT SELECT ON wellmgr.origin_subarea_relationships TO wios_migration
/
GRANT SELECT ON pedmgr.ped_fields TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortiums TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortium_details TO wios_migration
/
GRANT SELECT ON decmgr.xview_organisation_units TO wios_migration
/
GRANT SELECT ON decmgr.current_org_grp_organisations TO wios_migration
/
GRANT SELECT ON decmgr.current_organisation_groups TO wios_migration
/
GRANT SELECT ON pedmgr.ped_licences TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortiums TO wios_migration
/
GRANT SELECT ON pedmgr.ped_consortium_details TO wios_migration
/
GRANT SELECT ON decmgr.xview_organisation_units TO wios_migration
/
GRANT SELECT ON decmgr.xview_facility_fields TO wios_migration
/
GRANT SELECT ON devukmgr.fields TO wios_migration
/
GRANT SELECT ON decmgr.xview_facility_details TO wios_migration
/
GRANT SELECT ON spatialmgr.spatial_instance_periods TO wios_migration
/
GRANT SELECT ON spatialmgr.spatial_instance_details TO wios_migration
/
CREATE OR REPLACE VIEW wios_migration.wellbore_portal_data AS
  WITH licence_data AS (
    SELECT
      plm.id plm_id
    , pcdp.licence_type||pcdp.licence_no licence_type_number
    , cog.name subarea_operator_company_group
    , xou_admin.name licence_administrator
    , plbr.block_ref block
    , psa.title subarea_long_name
    , psa.status subarea_status
    , xou.name subarea_operator
    , COALESCE(sid_f.feature_offset_low_m, sid_f.feature_offset_min_m) strata_low
    , COALESCE(sid_f.feature_offset_high_m, sid_f.feature_offset_max_m) strata_high
    , es.id subarea_id
    FROM pedmgr.ped_licence_master plm -- licence ids for joining to WONS licence
    JOIN pedmgr.ped_current_data_points pcdp ON pcdp.licence_type = plm.licence_type AND pcdp.licence_no = plm.licence_no -- current position
    JOIN pedmgr.ped_licence_blocks plb ON plb.ped_dp_id = pcdp.id -- blocks on current position
    JOIN pedmgr.ped_licence_block_refs plbr ON plbr.plb_id = plb.id -- full block refs stored here
    JOIN pedmgr.ped_subareas psa ON psa.ped_lb_id = plb.id -- subareas for block
    JOIN spatialmgr.spatial_instance_periods sip ON sip.si_id = psa.si_id AND sip.status_control = 'C' -- get spatial data for subarea
    JOIN spatialmgr.spatial_instance_details sid_f ON sid_f.siv_id = sip.siv_id AND sid_f.class = 'F' -- single subarea may have multiple spatial features at different stratas, hence getting all features
    JOIN pedmgr.ped_consortiums pc ON pc.context_uref = psa.id || 'PEDLSA' AND pc.context_domain = 'SUBAREA_OPERATOR' -- operator entry type for subarea
    JOIN pedmgr.ped_consortium_details pcd ON pcd.ped_con_id = pc.id -- operator id for operator entry type
    JOIN decmgr.xview_organisation_units xou ON xou.organ_id = pcd.ou_id -- operator org name
    JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG' -- org group for operator id, REG used in portal, SDK is legacy
    JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id AND cog.org_grp_type = cogo.org_grp_type -- org group name
    JOIN pedmgr.ped_licences pl ON pl.ped_dp_id = pcdp.id -- licence record to join from data point to admin
    JOIN pedmgr.ped_consortiums pc_admin ON pc_admin.context_uref = pl.id || 'PEDLIC' AND pc_admin.context_domain = 'LICENCE_ADMINISTRATOR' -- admin entry type for licence
    JOIN pedmgr.ped_consortium_details pcd_admin ON pcd_admin.ped_con_id = pc_admin.id -- operator id for admin entry type
    JOIN decmgr.xview_organisation_units xou_admin ON xou_admin.organ_id = pcd_admin.ou_id -- lic admin org name
    JOIN pedmgr.epa_subareas es ON -- connecting subarea id with licence, block and subarea
        es.licence_id = plm.id
      AND es.block_reference = plbr.block_ref
      AND es.name = psa.title
    WHERE pcdp.ped_sim_id = 0 -- live licence only
    -- only current records at current position
    AND plb.end_datetime IS NULL
    AND psa.end_datetime IS NULL
    AND pc.end_datetime IS NULL
    AND pcd.end_datetime IS NULL
    AND pc_admin.end_datetime IS NULL
    AND pcd_admin.end_datetime IS NULL
  )
  , well_licence_data AS (
    SELECT DISTINCT -- distinct due to well origin view, and to get unique set of subarea strats for each subarea for later sum
      ld.licence_type_number
    , ld.subarea_operator_company_group
    , ld.licence_administrator
    , ld.block
    , ld.subarea_long_name
    , ld.strata_low
    , ld.strata_high
    , ld.subarea_operator
    , ewc.w_id wellbore_id
    , xwws.well_registration_no
    , xou.name wons_competent_operator
    , wms.mnem wons_mechanical_status
    -- convert to common unit of measure (metres) for comparison
    -- all depth is subsea so convert to negative values for comparison
    , CASE xwws.td_tvdss_unit
        WHEN 'FEET'
        THEN xwws.td_tvdss * 0.3048
        WHEN 'METRES'
        THEN xwws.td_tvdss
        ELSE NULL
      END * -1 well_total_depth_m
    FROM wellmgr.extant_wellbores_current ewc -- all current wellbores
    JOIN wellmgr.xview_wons_wellbore_search xwws ON xwws.wd_id = ewc.wd_id -- for general well information
    JOIN wellmgr.xview_wons_well_origin_search wo ON wo.wod_id = ewc.wod_id -- for origin information
    LEFT JOIN decmgr.xview_organisation_units xou ON xou.organ_id = xwws.competent_operator_ou_id -- competent operator org name
    LEFT JOIN wellmgr.wellbore_mechanical_statuses wms ON xwws.wellbore_mechanical_status = wms.mnem -- status of the well
    LEFT JOIN wellmgr.origin_subarea_relationships osr ON osr.well_origin_id = wo.wo_id -- spatial location of well based on origin, 0 or more matches
    LEFT JOIN licence_data ld -- licence data for spatial location of well
      ON  ld.plm_id = osr.gis_licence_master_id
      AND ld.subarea_id = osr.subarea_id
    WHERE wo.regulatory_jurisdiction = 'SEAWARD' -- check this with business team, if they want onshore wells then remove
    AND (ld.subarea_status IS NULL OR ld.subarea_status = 'EXTANT') -- orgin subarea relationship includes relinquished subareas, filter these out
  )
  , well_strata_matching AS (
    SELECT
      wld.licence_type_number
    , wld.subarea_operator_company_group
    , wld.licence_administrator
    , wld.block
    , wld.subarea_long_name
    , st.join(stagg(wld.strata_low||CASE WHEN wld.strata_low IS NULL OR wld.strata_high IS NULL THEN NULL ELSE ' to ' END||wld.strata_high), ', ', NULL, NULL, NULL, 'true', 'ORDER BY 1 DESC') subarea_stratas
    , COUNT(DISTINCT(wld.strata_low||wld.strata_high)) unique_strata_count
    , wld.subarea_operator
    , wld.wellbore_id
    , wld.well_registration_no
    , wld.wons_competent_operator
    , wld.wons_mechanical_status
    , wld.well_total_depth_m
    , SUM(
        CASE
          WHEN well_total_depth_m BETWEEN wld.strata_low AND wld.strata_high
          THEN 1
          ELSE 0
        END
      ) origin_strata_that_inc_well
    FROM well_licence_data wld
    GROUP BY
      wld.licence_type_number
    , wld.subarea_operator_company_group
    , wld.licence_administrator
    , wld.block
    , wld.subarea_long_name
    , wld.subarea_operator
    , wld.wellbore_id
    , wld.well_registration_no
    , wld.wons_competent_operator
    , wld.wons_mechanical_status
    , wld.well_total_depth_m
  )
  SELECT
    wsm.*
  , CASE
      -- The well TD depth is within every strata for the origin subarea.
      -- In this case you should be able to rely on the match returning a single subarea. However there are a few well origins that are in two blocks, possibly they are on the border of both.
      WHEN wsm.origin_strata_that_inc_well > 0 AND wsm.origin_strata_that_inc_well = wsm.unique_strata_count
        THEN 1
      -- The well TD is within a strata for the matching origin subarea but the subarea has multiple unique strata levels and it is not in all the stratas.
      -- In this case you can't be certain that the GIS match in the 2D plane is the same feature that matches the well td.
      WHEN wsm.origin_strata_that_inc_well > 0 AND wsm.origin_strata_that_inc_well < wsm.unique_strata_count
        THEN 2
      -- In this case the well origin is not in any extant subarea.
      WHEN wsm.subarea_long_name IS NULL
        THEN 3
      -- iIn this case there is no well TD information to perform a match on
      WHEN wsm.well_total_depth_m IS NULL
        THEN 4
      -- In this case the well TD depth does not match any of the strata for the origin subarea
      WHEN  wsm.origin_strata_that_inc_well = 0
        THEN 5
    END origin_subarea_match_well_td
  FROM well_strata_matching wsm
/
CREATE OR REPLACE VIEW wios_migration.installation_portal_data AS
  WITH licence_data AS (
    SELECT
      pf.field_id -- devuk field id for joining later
    , xou.name field_operator
    , cog.name field_operator_company_group
    , pcdp.licence_type||pcdp.licence_no licence_type_number
    , xou_admin.name licence_administrator
    FROM pedmgr.ped_current_data_points pcdp -- current position
    JOIN pedmgr.ped_fields pf ON pf.ped_dp_id = pcdp.id -- pears fields
    JOIN pedmgr.ped_consortiums pc ON pc.context_uref = pf.id || 'PEDFLD' AND pc.context_domain = 'FIELD_OPERATOR' -- operator entry type for field
    JOIN pedmgr.ped_consortium_details pcd ON pcd.ped_con_id = pc.id -- operator id for operator entry type
    JOIN decmgr.xview_organisation_units xou ON xou.organ_id = pcd.ou_id -- operator org name
    JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG' -- org group for operator id, REG used in portal, SDK is legacy
    JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id AND cog.org_grp_type = cogo.org_grp_type -- org group name
    JOIN pedmgr.ped_licences pl ON pl.ped_dp_id = pcdp.id -- licence record to join from data point to admin
    JOIN pedmgr.ped_consortiums pc_admin ON pc_admin.context_uref = pl.id || 'PEDLIC' AND pc_admin.context_domain = 'LICENCE_ADMINISTRATOR' -- admin entry type for licence
    JOIN pedmgr.ped_consortium_details pcd_admin ON pcd_admin.ped_con_id = pc_admin.id -- operator id for admin entry type
    JOIN decmgr.xview_organisation_units xou_admin ON xou_admin.organ_id = pcd_admin.ou_id -- lic admin org name
    WHERE pcdp.ped_sim_id = 0 -- live licence only
    -- only current records at current position
    AND pf.end_datetime IS NULL
    AND pc.end_datetime IS NULL
    AND pcd.end_datetime IS NULL
    AND pc_admin.end_datetime IS NULL
    AND pcd_admin.end_datetime IS NULL
  )
  SELECT
    xfd.f_id facility_id
  , xfd.facility_name installation_name
  , f.name field_name
  , ld.field_operator
  , ld.field_operator_company_group
  , ld.licence_type_number
  , ld.licence_administrator
  FROM decmgr.xview_facility_details xfd -- facility data
  LEFT JOIN decmgr.xview_facility_fields xff ON xff.fd_id = xfd.fd_id -- fields for facility where they exist, 0 or more matches
  LEFT JOIN devukmgr.fields f ON f.field_identifier = xff.ref_fld_id -- field info for related fields
  LEFT JOIN licence_data ld ON ld.field_id = f.field_identifier -- licence data for report
  WHERE xfd.status_control = 'C' -- current facility records only
/
CREATE OR REPLACE VIEW wios_migration.subarea_portal_data AS
  WITH licence_data AS (
    SELECT
      plm.id plm_id
    , pcdp.licence_type
    , pcdp.licence_no licence_number
    , cog.name subarea_operator_company_group
    , xou_admin.name licence_administrator
    , plbr.block_ref
    , psa.title subarea_long_name
    , xou.name subarea_operator
    , es.id subarea_id
    FROM pedmgr.ped_licence_master plm -- licence ids for joining to WONS licence
    JOIN pedmgr.ped_current_data_points pcdp ON pcdp.licence_type = plm.licence_type AND pcdp.licence_no = plm.licence_no -- current position
    JOIN pedmgr.ped_licence_blocks plb ON plb.ped_dp_id = pcdp.id -- blocks on current position
    JOIN pedmgr.ped_licence_block_refs plbr ON plbr.plb_id = plb.id -- full block refs stored here
    JOIN pedmgr.ped_subareas psa ON psa.ped_lb_id = plb.id -- subareas for block
    JOIN pedmgr.ped_consortiums pc ON pc.context_uref = psa.id || 'PEDLSA' AND pc.context_domain = 'SUBAREA_OPERATOR' -- operator entry type for subarea
    JOIN pedmgr.ped_consortium_details pcd ON pcd.ped_con_id = pc.id -- operator id for operator entry type
    JOIN decmgr.xview_organisation_units xou ON xou.organ_id = pcd.ou_id -- operator org name
    JOIN decmgr.current_org_grp_organisations cogo ON cogo.organ_id = xou.organ_id AND cogo.org_grp_type = 'REG' -- org group for operator id, REG used in portal, SDK is legacy
    JOIN decmgr.current_organisation_groups cog ON cog.id = cogo.org_grp_id AND cog.org_grp_type = cogo.org_grp_type -- org group name
    JOIN pedmgr.ped_licences pl ON pl.ped_dp_id = pcdp.id -- licence record to join from data point to admin
    JOIN pedmgr.ped_consortiums pc_admin ON pc_admin.context_uref = pl.id || 'PEDLIC' AND pc_admin.context_domain = 'LICENCE_ADMINISTRATOR' -- admin entry type for licence
    JOIN pedmgr.ped_consortium_details pcd_admin ON pcd_admin.ped_con_id = pc_admin.id -- operator id for admin entry type
    JOIN decmgr.xview_organisation_units xou_admin ON xou_admin.organ_id = pcd_admin.ou_id -- lic admin org name
    JOIN pedmgr.epa_subareas es ON -- connecting subarea id with licence, block and subarea
        es.licence_id = plm.id
      AND es.block_reference = plbr.block_ref
      AND es.name = psa.title
    WHERE pcdp.ped_sim_id = 0 -- live licence only
    -- only current records at current position
    AND plb.end_datetime IS NULL
    AND psa.end_datetime IS NULL
    AND psa.status = 'EXTANT'
    AND pc.end_datetime IS NULL
    AND pcd.end_datetime IS NULL
    AND pc_admin.end_datetime IS NULL
    AND pcd_admin.end_datetime IS NULL
  )
  SELECT
    ld.licence_type
  , ld.licence_number
  , ld.block_ref
  , ld.subarea_id
  , ld.subarea_long_name
  , ld.subarea_operator_company_group
  , ld.licence_administrator
  , ld.subarea_operator
  FROM licence_data ld -- licence data for report
/
