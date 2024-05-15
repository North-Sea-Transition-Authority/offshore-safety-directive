package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class ApplicantDetailFormService {

  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;
  private final ApplicantDetailFormValidator applicantDetailFormValidator;

  @Autowired
  ApplicantDetailFormService(ApplicantDetailPersistenceService applicantDetailPersistenceService,
                             ApplicantDetailFormValidator applicantDetailFormValidator) {
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
    this.applicantDetailFormValidator = applicantDetailFormValidator;
  }

  ApplicantDetailForm getForm(NominationDetail nominationDetail) {
    return applicantDetailPersistenceService.getApplicantDetail(nominationDetail)
        .map(this::applicantDetailEntityToForm)
        .orElseGet(ApplicantDetailForm::new);
  }

  BindingResult validate(ApplicantDetailForm form, BindingResult bindingResult) {
    applicantDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private ApplicantDetailForm applicantDetailEntityToForm(ApplicantDetail applicantDetail) {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(Objects.toString(applicantDetail.getPortalOrganisationId(), null));
    form.setApplicantReference(applicantDetail.getApplicantReference());
    return form;
  }
}
