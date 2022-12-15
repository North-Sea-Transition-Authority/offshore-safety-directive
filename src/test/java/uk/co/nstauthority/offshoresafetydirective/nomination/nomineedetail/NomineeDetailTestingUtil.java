package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class NomineeDetailTestingUtil {

  private NomineeDetailTestingUtil() {
    throw new IllegalStateException("NomineeDetailTestingUtil is an util class and should not be instantiated");
  }

  static NomineeDetailForm getValidNomineeDetailForm() {
    var localDate = LocalDate.now().plusYears(1L); //Make sure the date year is always in the future
    var validForm = new NomineeDetailForm();
    validForm.setNominatedOrganisationId(1);
    validForm.setReasonForNomination("reason");
    validForm.setPlannedStartDay("1");
    validForm.setPlannedStartMonth("1");
    validForm.setPlannedStartYear(String.valueOf(localDate.getYear()));
    validForm.setOperatorHasAuthority(true);
    validForm.setOperatorHasCapacity(true);
    validForm.setLicenseeAcknowledgeOperatorRequirements(true);
    return validForm;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 800;
    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();
    private Integer nominatedOrganisationId = 200;
    private String reasonForNomination = "reason for nomination";
    private LocalDate plannedStartDate = LocalDate.now();
    private Boolean operatorHasAuthority = true;
    private Boolean operatorHasCapacity = true;
    private Boolean licenseeAcknowledgeOperatorRequirements = true;

    private Builder() {
    }

    public Builder withId(Integer id) {
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
