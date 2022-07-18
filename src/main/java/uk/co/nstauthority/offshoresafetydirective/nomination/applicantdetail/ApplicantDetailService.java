package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class ApplicantDetailService {

  private final ApplicationDetailRepository applicationDetailRepository;
  private final ApplicantDetailFormValidator applicantDetailFormValidator;

  @Autowired
  ApplicantDetailService(
      ApplicationDetailRepository applicationDetailRepository,
      ApplicantDetailFormValidator applicantDetailFormValidator) {
    this.applicationDetailRepository = applicationDetailRepository;
    this.applicantDetailFormValidator = applicantDetailFormValidator;
  }

  @Transactional
  public ApplicantDetail createOrUpdateApplicantDetail(ApplicantDetailForm form, NominationDetail nominationDetail) {
    ApplicantDetail applicantDetail = applicationDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateApplicantDetail(nominationDetail, entity, form))
        .orElseGet(() -> createApplicantDetail(nominationDetail, form));
    applicationDetailRepository.save(applicantDetail);
    return applicantDetail;
  }

  ApplicantDetailForm getForm(NominationDetail nominationDetail) {
    return applicationDetailRepository.findByNominationDetail(nominationDetail)
        .map(this::applicantDetailEntityToForm)
        .orElseGet(ApplicantDetailForm::new);
  }

  BindingResult validate(ApplicantDetailForm form, BindingResult bindingResult) {
    applicantDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private ApplicantDetail createApplicantDetail(NominationDetail nominationDetail, ApplicantDetailForm form) {
    return new ApplicantDetail(
        nominationDetail,
        form.getPortalOrganisationId(),
        form.getApplicantReference()
    );
  }

  private ApplicantDetail updateApplicantDetail(NominationDetail nominationDetail,
                                                ApplicantDetail applicantDetail,
                                                ApplicantDetailForm form) {
    applicantDetail.setNominationDetail(nominationDetail);
    applicantDetail.setPortalOrganisationId(form.getPortalOrganisationId());
    applicantDetail.setApplicantReference(form.getApplicantReference());
    return applicantDetail;
  }

  private ApplicantDetailForm applicantDetailEntityToForm(ApplicantDetail applicantDetail) {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(applicantDetail.getPortalOrganisationId());
    form.setApplicantReference(applicantDetail.getApplicantReference());
    return form;
  }
}
