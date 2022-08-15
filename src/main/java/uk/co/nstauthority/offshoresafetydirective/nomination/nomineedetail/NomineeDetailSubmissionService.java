package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class NomineeDetailSubmissionService implements NominationSectionSubmissionService {

  private final NomineeDetailFormService nomineeDetailFormService;

  @Autowired
  NomineeDetailSubmissionService(NomineeDetailFormService nomineeDetailFormService) {
    this.nomineeDetailFormService = nomineeDetailFormService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var form = nomineeDetailFormService.getForm(nominationDetail);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = nomineeDetailFormService.validate(form, bindingResult);
    return !bindingResult.hasErrors();
  }
}
