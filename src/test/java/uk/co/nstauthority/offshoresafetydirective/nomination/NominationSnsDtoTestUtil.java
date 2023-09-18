package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Random;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

public class NominationSnsDtoTestUtil {

  private NominationSnsDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private NominationDetailId nominationDetailId = new NominationDetailId(UUID.randomUUID());
    private WellSelectionType wellSelectionType = WellSelectionType.NO_WELLS;
    private boolean hasInstallations = true;
    private ApplicantOrganisationId applicantOrganisationUnitId = new ApplicantOrganisationId(
        new Random().nextInt(Integer.MAX_VALUE)
    );
    private NominatedOrganisationId nominatedOrganisationUnitId = new NominatedOrganisationId(
        new Random().nextInt(Integer.MAX_VALUE)
    );

    private Builder() {
    }

    Builder withNominationDetailId(NominationDetailId nominationDetailId) {
      this.nominationDetailId = nominationDetailId;
      return this;
    }

    Builder withWellSelectionType(WellSelectionType wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
      return this;
    }

    Builder withHasInstallations(boolean hasInstallations) {
      this.hasInstallations = hasInstallations;
      return this;
    }

    Builder withApplicantOrganisationUnitId(ApplicantOrganisationId applicantOrganisationUnitId) {
      this.applicantOrganisationUnitId = applicantOrganisationUnitId;
      return this;
    }

    Builder withNominatedOrganisationUnitId(NominatedOrganisationId nominatedOrganisationUnitId) {
      this.nominatedOrganisationUnitId = nominatedOrganisationUnitId;
      return this;
    }

    public NominationSnsDto build() {
      return new NominationSnsDto(
          nominationDetailId.id(),
          wellSelectionType,
          hasInstallations,
          applicantOrganisationUnitId.id(),
          nominatedOrganisationUnitId.id()
      );
    }

  }

}