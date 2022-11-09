package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class RelatedInformationSubmissionService implements NominationSectionSubmissionService {

  private final RelatedInformationFormService relatedInformationFormService;
  private final RelatedInformationValidator relatedInformationValidator;

  @Autowired
  RelatedInformationSubmissionService(
      RelatedInformationFormService relatedInformationFormService,
      RelatedInformationValidator relatedInformationValidator) {
    this.relatedInformationFormService = relatedInformationFormService;
    this.relatedInformationValidator = relatedInformationValidator;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var form = relatedInformationFormService.getForm(nominationDetail);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    relatedInformationValidator.validate(form, bindingResult);
    return !bindingResult.hasErrors();
  }

  @Override
  public void onSubmission(NominationDetail nominationDetail) {
    NominationSectionSubmissionService.super.onSubmission(nominationDetail);
  }

}
