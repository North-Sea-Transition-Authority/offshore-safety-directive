package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.APPOINTMENTS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.ASSETS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class AppointmentQueryService {

  private final DSLContext dslContext;

  @Autowired
  AppointmentQueryService(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  List<AppointmentQueryResultItemDto> search(Collection<PortalAssetType> portalAssetTypeRestrictions,
                                             SystemOfRecordSearchFilter searchFilter) {
    return dslContext
        .select(
            ASSETS.PORTAL_ASSET_ID,
            ASSETS.PORTAL_ASSET_TYPE,
            APPOINTMENTS.ID,
            APPOINTMENTS.APPOINTED_PORTAL_OPERATOR_ID,
            APPOINTMENTS.TYPE,
            APPOINTMENTS.RESPONSIBLE_FROM_DATE,
            ASSETS.ASSET_NAME
        )
        .from(ASSETS)
        .join(APPOINTMENTS)
        .on(APPOINTMENTS.ASSET_ID.eq(ASSETS.ID))
        .where(getPredicateConditions(portalAssetTypeRestrictions, searchFilter))
        .fetchInto(AppointmentQueryResultItemDto.class);
  }


  private List<Condition> getPredicateConditions(Collection<PortalAssetType> portalAssetTypeRestrictions,
                                                 SystemOfRecordSearchFilter searchFilter) {

    List<Condition> predicateList = new ArrayList<>();

    // only current appointments
    predicateList.add(APPOINTMENTS.RESPONSIBLE_TO_DATE.isNull());

    // only appointments which haven't been removed
    predicateList.add(APPOINTMENTS.STATUS.in(AppointmentAccessService.ACTIVE_STATUSES));

    // if operator ID filter provided then filter by appointed operator
    if (searchFilter.appointedOperatorId() != null) {
      predicateList.add(
          APPOINTMENTS.APPOINTED_PORTAL_OPERATOR_ID.eq(String.valueOf(searchFilter.appointedOperatorId()))
      );
    }

    // if wellbore ID filter provided then filter by wellbore ID and wellbore type
    if (!CollectionUtils.isEmpty(searchFilter.wellboreIds())) {

      List<String> wellboreIdStrings = searchFilter.wellboreIds()
          .stream()
          .map(String::valueOf)
          .toList();

      predicateList.add(ASSETS.PORTAL_ASSET_ID.in(wellboreIdStrings));
      // as different portal assets could have the same ID ensure the restriction list is only wellbore
      portalAssetTypeRestrictions = Set.of(PortalAssetType.WELLBORE);
    }

    // if installation ID filter provided then filter by installation ID and installation type
    if (searchFilter.installationId() != null) {
      predicateList.add(ASSETS.PORTAL_ASSET_ID.eq(String.valueOf(searchFilter.installationId())));
      // as different portal assets could have the same ID ensure the restriction list is only installation
      portalAssetTypeRestrictions = Set.of(PortalAssetType.INSTALLATION);
    }

    if (searchFilter.subareaId() != null) {
      predicateList.add(ASSETS.PORTAL_ASSET_ID.eq(searchFilter.subareaId()));
      portalAssetTypeRestrictions = Set.of(PortalAssetType.SUBAREA);
    }

    // filter only asset types which match the required types

    List<String> portalAssetTypeRestrictionNames = portalAssetTypeRestrictions
        .stream()
        .map(PortalAssetType::name)
        .toList();

    predicateList.add(ASSETS.PORTAL_ASSET_TYPE.in(portalAssetTypeRestrictionNames));

    return predicateList;
  }
}