package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.portalorganisation.PortalOrganisationRestController;

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

  ModelAndView getApplicantDetailsModelAndView(ApplicantDetailForm form) {
    return new ModelAndView("osd/nomination/applicantdetails/applicantDetails")
        .addObject("form", form)
        .addObject("portalOrganisationsRestUrl", getPortalOrganisationSearchUrl())
        .addObject(
            "actionUrl",
            ReverseRouter.route(on(ApplicantDetailController.class).saveApplicantDetails(form, null))
        )
        .addObject("backLinkUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()));
  }

  @Transactional
  public ApplicantDetail createApplicantDetail(ApplicantDetailForm form, NominationDetail detail) {
    var applicantDetail = new ApplicantDetail(
        detail,
        form.getPortalOrganisationId(),
        form.getApplicantReference()
    );
    applicationDetailRepository.save(applicantDetail);
    return applicantDetail;
  }

  BindingResult validate(ApplicantDetailForm form, BindingResult bindingResult) {
    applicantDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private String getPortalOrganisationSearchUrl() {
    return PortalOrganisationRestController.route(on(PortalOrganisationRestController.class).searchPortalOrganisations(null));
  }
}
