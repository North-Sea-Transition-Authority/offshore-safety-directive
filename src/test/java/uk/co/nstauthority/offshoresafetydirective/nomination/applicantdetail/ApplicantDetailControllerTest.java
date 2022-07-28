package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;


@WebMvcTest
@ContextConfiguration(classes = ApplicantDetailController.class)
@WithMockUser
class ApplicantDetailControllerTest extends AbstractControllerTest {

  private final int NOMINATION_ID = 1;

  private final NominationDetail nominationDetail = NominationDetailTestUtil.getNominationDetail();

  @MockBean
  private ApplicantDetailService applicantDetailService;

  @MockBean
  private NominationService nominationService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Test
  void getNewApplicantDetails_assertModelProperties() throws Exception {
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(ApplicantDetailController.class).getNewApplicantDetails()))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView.getModel()).containsOnlyKeys(
        "form",
        "portalOrganisationsRestUrl",
        "actionUrl",
        "backLinkUrl",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.form",
        "navigationItems",
        "currentEndPoint"
    );

    var expectedPortalOrganisationsRestUrl =
        RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchPortalOrganisations(null));
    var expectedActionUrl = ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(null, null));
    var expectedBackLinkUrl = ReverseRouter.route(on(StartNominationController.class).startNomination());

    assertThat(modelAndView.getModel()).containsAllEntriesOf(Map.of(
        "portalOrganisationsRestUrl", expectedPortalOrganisationsRestUrl,
        "actionUrl", expectedActionUrl,
        "backLinkUrl", expectedBackLinkUrl
    ));

    assertEquals("osd/nomination/applicantdetails/applicantDetails", modelAndView.getViewName());
  }

  @Test
  void createApplicantDetails_whenValidForm_assertRedirection() throws Exception {
    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
    var applicationDetail = new ApplicantDetail(1);
    applicationDetail.setPortalOrganisationId(form.getPortalOrganisationId());
    applicationDetail.setApplicantReference(form.getApplicantReference());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationService.startNomination()).thenReturn(nominationDetail);
    when(applicantDetailService.createOrUpdateApplicantDetail(any(), eq(nominationDetail))).thenReturn(applicationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(form, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(nominationService, times(1)).startNomination();
    verify(applicantDetailService, times(1)).createOrUpdateApplicantDetail(any(), eq(nominationDetail));
  }

  @Test
  void createApplicantDetails_whenInvalidForm_assertOk() throws Exception {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(applicantDetailService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(form, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nominationService, never()).startNomination();
    verify(applicantDetailService, never()).createOrUpdateApplicantDetail(any(), any());
  }

  @Test
  void getUpdateApplicantDetails_assertModelProperties() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(applicantDetailService.getForm(nominationDetail)).thenReturn(ApplicantDetailTestUtil.getValidApplicantDetailForm());
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ApplicantDetailController.class).getUpdateApplicantDetails(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView.getModel()).containsOnlyKeys(
        "form",
        "preselectedItems",
        "portalOrganisationsRestUrl",
        "actionUrl",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "breadcrumbsList",
        "currentPage",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.form",
        "navigationItems",
        "currentEndPoint"
    );

    var expectedPortalOrganisationsRestUrl =
        RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchPortalOrganisations(null));
    var expectedActionUrl =
        ReverseRouter.route(on(ApplicantDetailController.class).updateApplicantDetails(NOMINATION_ID, null, null));

    assertThat(modelAndView.getModel()).containsAllEntriesOf(Map.of(
        "portalOrganisationsRestUrl", expectedPortalOrganisationsRestUrl,
        "actionUrl", expectedActionUrl
    ));

    assertEquals("osd/nomination/applicantdetails/applicantDetails", modelAndView.getViewName());
  }

  @Test
  void updateApplicantDetails_whenValidForm_assertRedirection() throws Exception {
    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
    var applicationDetail = new ApplicantDetail(1);
    applicationDetail.setPortalOrganisationId(form.getPortalOrganisationId());
    applicationDetail.setApplicantReference(form.getApplicantReference());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(applicantDetailService.createOrUpdateApplicantDetail(any(), eq(nominationDetail))).thenReturn(applicationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).updateApplicantDetails(NOMINATION_ID, form, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(applicantDetailService, times(1)).createOrUpdateApplicantDetail(any(), eq(nominationDetail));
    verify(nominationService, never()).startNomination();
  }

  @Test
  void updateApplicantDetails_whenInvalidForm_assertOk() throws Exception {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(applicantDetailService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).updateApplicantDetails(NOMINATION_ID, form, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nominationService, never()).startNomination();
    verify(applicantDetailService, never()).createOrUpdateApplicantDetail(any(), any());
  }
}