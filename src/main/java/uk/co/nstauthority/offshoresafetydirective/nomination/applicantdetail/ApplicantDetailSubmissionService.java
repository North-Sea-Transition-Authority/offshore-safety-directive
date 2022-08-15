package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class ApplicantDetailSubmissionService implements NominationSectionSubmissionService {

  private final ApplicantDetailFormService applicantDetailFormService;

  @Autowired
  ApplicantDetailSubmissionService(ApplicantDetailFormService applicantDetailFormService) {
    this.applicantDetailFormService = applicantDetailFormService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var form = applicantDetailFormService.getForm(nominationDetail);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = applicantDetailFormService.validate(form, bindingResult);
    return !bindingResult.hasErrors();
  }
}
