package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.portalorganisation.PortalOrganisationRestController;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailServiceTest {

  private final NominationDetail nominationDetail = NominationDetailUtil.getNominationDetail();

  private static ApplicationDetailRepository applicationDetailRepository;
  private static ApplicantDetailFormValidator applicantDetailFormValidator;

  private static ApplicantDetailService applicantDetailService;

  @BeforeAll
  static void setup() {
    applicationDetailRepository = mock(ApplicationDetailRepository.class);
    applicantDetailFormValidator = mock(ApplicantDetailFormValidator.class);
    applicantDetailService = new ApplicantDetailService(applicationDetailRepository, applicantDetailFormValidator);
  }

  @Test
  void getApplicantDetailsModelAndView_assertModelProperties() {
    var form = new ApplicantDetailForm();
    var expectedPortalOrganisationsRestUrl =
        PortalOrganisationRestController.route(on(PortalOrganisationRestController.class).searchPortalOrganisations(null));
    var expectedActionUrl = ReverseRouter.route(on(ApplicantDetailController.class).saveApplicantDetails(
        form,
        null
    ));

    var model = applicantDetailService.getApplicantDetailsModelAndView(form);

    assertThat(model.getModelMap()).containsExactly(
        entry("form", form),
        entry("portalOrganisationsRestUrl", expectedPortalOrganisationsRestUrl),
        entry("actionUrl", expectedActionUrl),
        entry("backLinkUrl", ReverseRouter.route(on(StartNominationController.class).startNomination()))
    );
    assertEquals("osd/nomination/applicantdetails/applicantDetails", model.getViewName());
  }

  @Test
  void createApplicantDetail_whenGivenAForm_verifyCreatedEntity() {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(1);
    form.setApplicantReference("ref#1");
    var applicantDetailArgumentCaptor = ArgumentCaptor.forClass(ApplicantDetail.class);

    applicantDetailService.createApplicantDetail(form, nominationDetail);

    verify(applicationDetailRepository, times(1)).save(applicantDetailArgumentCaptor.capture());
    var savedApplicantDetail = (ApplicantDetail) applicantDetailArgumentCaptor.getValue();
    
    assertThat(savedApplicantDetail)
        .extracting(
            ApplicantDetail::getPortalOrganisationId,
            ApplicantDetail::getApplicantReference,
            ApplicantDetail::getNominationDetail
        )
        .containsExactly(
            form.getPortalOrganisationId(),
            form.getApplicantReference(),
            nominationDetail
        );
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    applicantDetailService.validate(form, bindingResult);

    verify(applicantDetailFormValidator, times(1)).validate(form, bindingResult);
  }
}