package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailPersistenceService;

@Service
class NominationSubmissionFormValidator {

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final Clock clock;

  @Autowired
  NominationSubmissionFormValidator(NomineeDetailPersistenceService nomineeDetailPersistenceService, Clock clock) {
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.clock = clock;
  }

  public void validate(NominationSubmissionForm form, BindingResult bindingResult, NominationDetail nominationDetail) {
    var confirmedAuthority = BooleanUtils.toBooleanObject(form.getConfirmedAuthority());
    if (BooleanUtils.isNotTrue(confirmedAuthority)) {
      bindingResult.rejectValue(
          "confirmedAuthority",
          "confirmedAuthority.required",
          "You must confirm that you have authority to submit the nomination"
      );
    }

    if (isNominationWithinFastTrackPeriod(nominationDetail)) {
      StringInputValidator.builder().validate(form.getReasonForFastTrack(), bindingResult);
    }
  }

  public boolean isNominationWithinFastTrackPeriod(NominationDetail nominationDetail) {
    var nomineeDetail = nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)
        .orElseThrow(() -> new IllegalStateException(
            "Unexpected submission of NominationDetail [%s] when no NomineeDetail exists".formatted(
                nominationDetail.getId()
            )
        ));
    var appointmentPlannedDate = nomineeDetail.getPlannedStartDate();
    var threeMonthsFromCurrentDate = LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()).plusMonths(3);
    return appointmentPlannedDate.isBefore(threeMonthsFromCurrentDate);
  }

}
