package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.opensaml.saml.metadata.resolver.impl.TemplateRequestURLBuilder.EncodingStyle.form;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicantDetailController.class)
class ApplicantDetailControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String GET_NEW_APPLICANT_DETAILS_ROUTE =  ReverseRouter.route(
      on(ApplicantDetailController.class).getNewApplicantDetails());

  private static final String POST_CREATE_APPLICANT_DETAILS_ROUTE =  ReverseRouter.route(
      on(ApplicantDetailController.class).createApplicantDetails(null, null));

  @MockBean
  private ApplicantDetailFormService applicantDetailFormService;

  @MockBean
  private NominationService nominationService;

  @SecurityTest
  void getNewApplicantDetails_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(get(GET_NEW_APPLICANT_DETAILS_ROUTE))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void createApplicantDetails_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(post(POST_CREATE_APPLICANT_DETAILS_ROUTE)
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Nested
  class GetNewApplicantDetails {

    @SecurityTest
    void whenUserCanStartNomination() throws Exception {

      givenUserCanStartNomination(USER.wuaId());

      mockMvc.perform(get(GET_NEW_APPLICANT_DETAILS_ROUTE)
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/applicantdetails/applicantDetails"))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchOrganisationsRelatedToUser(null, OrganisationFilterType.ACTIVE.name(), null))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(null, null))
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(StartNominationController.class).startNomination())
        ));
    }

    @SecurityTest
    void whenUserCannotStartNomination() throws Exception {

      givenUserCannotStartNomination(USER.wuaId());

      mockMvc.perform(get(GET_NEW_APPLICANT_DETAILS_ROUTE)
          .with(user(USER)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class CreateApplicantDetails {

    @SecurityTest
    void whenUserCanStartNomination() throws Exception {

      givenUserCanStartNomination(USER.wuaId());

      var bindingResult = new BeanPropertyBindingResult(form, "form");

      when(applicantDetailFormService.validate(any(), any()))
          .thenReturn(bindingResult);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      when(nominationService.startNomination())
          .thenReturn(nominationDetail);

      var taskListUrl = ReverseRouter.route(on(NominationTaskListController.class)
          .getTaskList(new NominationId(nominationDetail)));

      mockMvc.perform(post(POST_CREATE_APPLICANT_DETAILS_ROUTE)
          .with(csrf())
          .with(user(USER)))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(taskListUrl));
    }

    @SecurityTest
    void whenUserCannotStartNomination() throws Exception {

      givenUserCannotStartNomination(USER.wuaId());

      mockMvc.perform(post(POST_CREATE_APPLICANT_DETAILS_ROUTE)
          .with(csrf())
          .with(user(USER)))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenInvalidForm() throws Exception {

      givenUserCanStartNomination(USER.wuaId());

      var form = new ApplicantDetailForm();

      var bindingResult = new BeanPropertyBindingResult(form, "form");
      bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

      when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

      mockMvc.perform(post(POST_CREATE_APPLICANT_DETAILS_ROUTE)
          .with(csrf())
          .with(user(USER)))
          .andExpect(status().isOk());

      verify(nominationService, never()).startNomination();
      verify(applicantDetailPersistenceService, never()).createOrUpdateApplicantDetail(any(), any());
    }

    @Test
    void whenValidForm() throws Exception {

      givenUserCanStartNomination(USER.wuaId());

      var form = new ApplicantDetailForm();

      var bindingResult = new BeanPropertyBindingResult(form, "form");

      when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      when(nominationService.startNomination()).thenReturn(nominationDetail);

      var taskListUrl = ReverseRouter.route(on(NominationTaskListController.class)
          .getTaskList(new NominationId(nominationDetail)));

      mockMvc.perform(post(POST_CREATE_APPLICANT_DETAILS_ROUTE)
          .with(csrf())
          .with(user(USER)))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(taskListUrl));

      verify(nominationService).startNomination();

      verify(applicantDetailPersistenceService)
          .createOrUpdateApplicantDetail(any(ApplicantDetailForm.class), eq(nominationDetail));
    }
  }

  @Nested
  class GetUpdateApplicantDetails {

    private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    private static final NominationId NOMINATION_ID = new NominationId(NOMINATION_DETAIL.getNomination().getId());

    private static final String GET_UPDATE_APPLICANT_DETAILS_ROUTE = ReverseRouter.route(
        on(ApplicantDetailController.class).getUpdateApplicantDetails(NOMINATION_ID)
    );

    @SecurityTest
    void whenUserNotLoggedIn() throws Exception {
      mockMvc.perform(get(GET_UPDATE_APPLICANT_DETAILS_ROUTE))
          .andExpect(redirectionToLoginUrl());
    }

    @SecurityTest
    void onlyDraftStatusPermitted() {

      var nominationDetail = NominationDetailTestUtil.builder()
          .withNominationId(NOMINATION_ID)
          .build();

      givenLatestNominationDetail(nominationDetail);

      givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

      var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
      when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(form);

      NominationStatusSecurityTestUtil.smokeTester(mockMvc)
          .withPermittedNominationStatus(NominationStatus.DRAFT)
          .withNominationDetail(nominationDetail)
          .withUser(USER)
          .withGetEndpoint(GET_UPDATE_APPLICANT_DETAILS_ROUTE)
          .test();
    }

    @SecurityTest
    void whenUserHasNoRoleInApplicantTeamForDraftNominationAccess() throws Exception {

      givenLatestNominationDetail(NOMINATION_DETAIL);

      givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

      mockMvc.perform(get(GET_UPDATE_APPLICANT_DETAILS_ROUTE)
          .with(user(USER)))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenUserHasRoleInApplicantTeamForDraftNominationAccess() throws Exception {

      givenLatestNominationDetail(NOMINATION_DETAIL);

      givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

      var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
      when(applicantDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(form);

      var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .withName("name")
          .withRegisteredNumber("registered number")
          .build();

      when(portalOrganisationUnitQueryService.getOrganisationById(
          Integer.valueOf(form.getPortalOrganisationId()),
          ApplicantDetailController.PRE_SELECTED_APPLICANT_ORGANISATION_PURPOSE
      ))
          .thenReturn(Optional.of(portalOrganisationUnit));

      mockMvc.perform(get(GET_UPDATE_APPLICANT_DETAILS_ROUTE)
          .with(user(USER)))
          .andExpect(status().isOk())
          .andExpect(view().name("osd/nomination/applicantdetails/applicantDetails"))
          .andExpect(model().attribute(
              "portalOrganisationsRestUrl",
              RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                  .searchOrganisationsRelatedToUser(null, OrganisationFilterType.ACTIVE.name(), null))
          ))
          .andExpect(model().attribute(
              "actionUrl",
              ReverseRouter.route(on(ApplicantDetailController.class).updateApplicantDetails(NOMINATION_ID, null, null))
          ))
          .andExpect(model().attribute(
              "preselectedItems",
              Map.of("10", "name (registered number)")
          ))
          .andExpect(model().attribute(
              "breadcrumbsList",
              Map.of(
                  ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                  WorkAreaController.WORK_AREA_TITLE,
                  ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)),
                  NominationTaskListController.PAGE_NAME
              )
          ))
          .andExpect(model().attribute("currentPage", ApplicantDetailController.PAGE_NAME));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"not-a-number"})
    void whenInvalidPreselectedOrganisation(String nonIntegerValue) throws Exception {

      givenLatestNominationDetail(NOMINATION_DETAIL);

      givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

      var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
      form.setPortalOrganisationId(nonIntegerValue);

      when(applicantDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(form);

      mockMvc.perform(get(GET_UPDATE_APPLICANT_DETAILS_ROUTE)
          .with(user(USER)))
          .andExpect(status().isOk())
          .andExpect(model().attribute("preselectedItems", Map.of()));

      verifyNoInteractions(portalOrganisationUnitQueryService);
    }
  }

  @Nested
  class UpdateApplicantDetails {

    private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    private static final NominationId NOMINATION_ID = new NominationId(NOMINATION_DETAIL.getNomination().getId());

    private static final String POST_UPDATE_APPLICANT_DETAILS_ROUTE = ReverseRouter.route(
        on(ApplicantDetailController.class).updateApplicantDetails(NOMINATION_ID, null, null)
    );

    @SecurityTest
    void whenUserNotLoggedIn() throws Exception {
      mockMvc.perform(post(POST_UPDATE_APPLICANT_DETAILS_ROUTE)
          .with(csrf()))
          .andExpect(redirectionToLoginUrl());
    }

    @SecurityTest
    void onlyDraftStatusPermitted() {

      var nominationDetail = NominationDetailTestUtil.builder()
          .withNominationId(NOMINATION_ID)
          .build();

      givenLatestNominationDetail(nominationDetail);

      givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

      var bindingResult = new BeanPropertyBindingResult(form, "form");
      when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

      NominationStatusSecurityTestUtil.smokeTester(mockMvc)
          .withPermittedNominationStatus(NominationStatus.DRAFT)
          .withNominationDetail(nominationDetail)
          .withUser(USER)
          .withPostEndpoint(POST_UPDATE_APPLICANT_DETAILS_ROUTE, status().is3xxRedirection(), status().isForbidden())
          .test();
    }

    @Test
    void whenInvalidForm() throws Exception {

      givenLatestNominationDetail(NOMINATION_DETAIL);

      givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

      var bindingResult = new BeanPropertyBindingResult(form, "form");
      bindingResult.addError(new FieldError("form", "field", "message"));

      when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

      mockMvc.perform(post(POST_UPDATE_APPLICANT_DETAILS_ROUTE)
          .with(user(USER))
          .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(view().name("osd/nomination/applicantdetails/applicantDetails"));

      verify(nominationService, never()).startNomination();
      verify(applicantDetailPersistenceService, never()).createOrUpdateApplicantDetail(any(), any());
    }

    @Test
    void whenValidForm() throws Exception {

      givenLatestNominationDetail(NOMINATION_DETAIL);

      givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

      var bindingResult = new BeanPropertyBindingResult(form, "form");

      when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

      mockMvc.perform(post(POST_UPDATE_APPLICANT_DETAILS_ROUTE)
          .with(user(USER))
          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectionToTaskList(NOMINATION_ID));

      verify(nominationService, never()).startNomination();
      verify(applicantDetailPersistenceService).createOrUpdateApplicantDetail(any(), eq(NOMINATION_DETAIL));
    }
  }
}