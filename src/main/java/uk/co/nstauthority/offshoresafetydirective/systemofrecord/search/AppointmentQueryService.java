package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class AppointmentQueryService {

  private final DSLContext dslContext;

  @Autowired
  AppointmentQueryService(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  List<AppointmentQueryResultItemDto> search(Collection<PortalAssetType> portalAssetTypeRestrictions,
                                             SystemOfRecordSearchForm searchForm) {
    return dslContext
        .select(
            field("assets.portal_asset_id"),
            field("assets.portal_asset_type"),
            field("appointments.id"),
            field("appointments.appointed_portal_operator_id"),
            field("appointments.type"),
            field("appointments.responsible_from_date"),
            field("assets.asset_name")
        )
        .from(table("assets"))
        .join(table("appointments"))
          .on(field("appointments.asset_id").eq(field("assets.id")))
        .where(getPredicateConditions(portalAssetTypeRestrictions, searchForm))
        .fetchInto(AppointmentQueryResultItemDto.class);
  }


  private List<Condition> getPredicateConditions(Collection<PortalAssetType> portalAssetTypeRestrictions,
                                                 SystemOfRecordSearchForm searchForm) {

    List<Condition> predicateList = new ArrayList<>();

    // only active appointments
    predicateList.add(field("appointments.responsible_to_date").isNull());

    // if operator ID filter provided then filter by appointed operator
    if (searchForm.getAppointedOperatorId() != null) {
      predicateList.add(
          field("appointments.appointed_portal_operator_id").eq(String.valueOf(searchForm.getAppointedOperatorId()))
      );
    }

    // if wellbore ID filter provided then filter by wellbore ID and wellbore type
    if (searchForm.getWellboreId() != null) {
      predicateList.add(
          field("assets.portal_asset_id").eq(String.valueOf(searchForm.getWellboreId()))
      );
      // as different portal assets could have the same ID ensure the restriction list is only wellbore
      portalAssetTypeRestrictions = Set.of(PortalAssetType.WELLBORE);
    }

    // filter only asset types which match the required types
    predicateList.add(field("assets.portal_asset_type")
        .in(portalAssetTypeRestrictions.stream().map(PortalAssetType::name).toList()));

    // if operator ID filter provided then filter by appointed operator
    if (searchForm.getAppointedOperatorId() != null) {
      predicateList.add(
          field("appointments.appointed_portal_operator_id").eq(String.valueOf(searchForm.getAppointedOperatorId()))
      );
    }

    return predicateList;
  }
}
