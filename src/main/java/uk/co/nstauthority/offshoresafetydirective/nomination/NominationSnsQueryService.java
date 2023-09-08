package uk.co.nstauthority.offshoresafetydirective.nomination;

import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.APPLICANT_DETAILS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.INSTALLATION_INCLUSION;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.NOMINATION_DETAILS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.NOMINEE_DETAILS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.WELL_SELECTION_SETUP;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class NominationSnsQueryService {

  private final DSLContext context;

  @Autowired
  NominationSnsQueryService(DSLContext context) {
    this.context = context;
  }

  NominationSnsDto getNominationSnsDto(NominationDetail nominationDetail) {
    return context
        .select(
            NOMINATION_DETAILS.ID,
            WELL_SELECTION_SETUP.SELECTION_TYPE,
            INSTALLATION_INCLUSION.INCLUDE_INSTALLATIONS_IN_NOMINATION,
            APPLICANT_DETAILS.PORTAL_ORGANISATION_ID,
            NOMINEE_DETAILS.NOMINATED_ORGANISATION_ID
        )
        .from(NOMINATION_DETAILS)
        .join(WELL_SELECTION_SETUP).on(WELL_SELECTION_SETUP.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .join(INSTALLATION_INCLUSION).on(INSTALLATION_INCLUSION.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .join(APPLICANT_DETAILS).on(APPLICANT_DETAILS.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .join(NOMINEE_DETAILS).on(NOMINEE_DETAILS.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .where(NOMINATION_DETAILS.ID.eq(nominationDetail.getId()))
        .fetchOneInto(NominationSnsDto.class);
  }
}
