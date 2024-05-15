package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NomineeDetailTestingUtil {

  private NomineeDetailTestingUtil() {
    throw new IllegalStateException("NomineeDetailTestingUtil is an util class and should not be instantiated");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();
    private Integer nominatedOrganisationId = 200;
    private String reasonForNomination = "reason for nomination";
    private LocalDate plannedStartDate = LocalDate.now();
    private Boolean operatorHasAuthority = true;
    private Boolean operatorHasCapacity = true;
    private Boolean licenseeAcknowledgeOperatorRequirements = true;

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withNominatedOrganisationId(Integer nominatedOrganisationId) {
      this.nominatedOrganisationId = nominatedOrganisationId;
      return this;
    }

    public Builder withReasonForNomination(String reasonForNomination) {
      this.reasonForNomination = reasonForNomination;
      return this;
    }

    public Builder withPlannedStartDate(LocalDate plannedStartDate) {
      this.plannedStartDate = plannedStartDate;
      return this;
    }

    public Builder withOperatorHasAuthority(Boolean operatorHasAuthority) {
      this.operatorHasAuthority = operatorHasAuthority;
      return this;
    }

    public Builder withOperatorHasCapacity(Boolean operatorHasCapacity) {
      this.operatorHasCapacity = operatorHasCapacity;
      return this;
    }

    public Builder withLicenseeAcknowledgeOperatorRequirements(Boolean licenseeAcknowledgeOperatorRequirements) {
      this.licenseeAcknowledgeOperatorRequirements = licenseeAcknowledgeOperatorRequirements;
      return this;
    }

    public NomineeDetail build() {
      var nomineeDetail = new NomineeDetail(id);
      nomineeDetail.setNominationDetail(nominationDetail);
      nomineeDetail.setNominatedOrganisationId(nominatedOrganisationId);
      nomineeDetail.setReasonForNomination(reasonForNomination);
      nomineeDetail.setPlannedStartDate(plannedStartDate);
      nomineeDetail.setOperatorHasAuthority(operatorHasAuthority);
      nomineeDetail.setOperatorHasCapacity(operatorHasCapacity);
      nomineeDetail.setLicenseeAcknowledgeOperatorRequirements(licenseeAcknowledgeOperatorRequirements);
      return nomineeDetail;
    }
  }
}
