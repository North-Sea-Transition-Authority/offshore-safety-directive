package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;

class NomineeDetailTestingUtil {

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
}
