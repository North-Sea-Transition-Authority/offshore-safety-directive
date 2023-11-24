package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;


@ContextConfiguration(classes = ApplicantDetailController.class)
class ApplicantDetailControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
      .build();

  private static final NominationId nominationId = new NominationId(UUID.randomUUID());

  private final NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
      .withNominationId(nominationId)
      .withStatus(NominationStatus.DRAFT)
      .build();

  @MockBean
  private ApplicantDetailFormService applicantDetailFormService;

  @MockBean
  private NominationService nominationService;

  @MockBean
  PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @BeforeEach
  void setup() {
    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(ApplicantDetailController.class).getUpdateApplicantDetails(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(ApplicantDetailController.class)
                .updateApplicantDetails(nominationId, form, bindingResult)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    when(nominationService.startNomination()).thenReturn(nominationDetail);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(ApplicantDetailController.class).getUpdateApplicantDetails(nominationId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(ApplicantDetailController.class)
                .updateApplicantDetails(nominationId, form, bindingResult)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withGetEndpoint(
            ReverseRouter.route(on(ApplicantDetailController.class).getNewApplicantDetails())
        )
        .withPostEndpoint(
            ReverseRouter.route(on(ApplicantDetailController.class)
                .createApplicantDetails(null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getNewApplicantDetails_assertModelProperties() throws Exception {

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicantDetailController.class).getNewApplicantDetails()))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/applicantdetails/applicantDetails"))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null))
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

  @Test
  void createApplicantDetails_whenValidForm_assertRedirection() throws Exception {
    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
    var applicationDetail = new ApplicantDetail(UUID.randomUUID());
    applicationDetail.setPortalOrganisationId(Integer.valueOf(form.getPortalOrganisationId()));
    applicationDetail.setApplicantReference(form.getApplicantReference());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationService.startNomination()).thenReturn(nominationDetail);
    when(applicantDetailPersistenceService.createOrUpdateApplicantDetail(any(), eq(nominationDetail)))
        .thenReturn(applicationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(form, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId))));

    verify(nominationService, times(1)).startNomination();
    verify(applicantDetailPersistenceService, times(1)).createOrUpdateApplicantDetail(any(), eq(nominationDetail));
  }

  @Test
  void createApplicantDetails_whenInvalidForm_assertOk() throws Exception {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicantDetailController.class).createApplicantDetails(form, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk());

    verify(nominationService, never()).startNomination();
    verify(applicantDetailPersistenceService, never()).createOrUpdateApplicantDetail(any(), any());
  }

  @Test
  void getUpdateApplicantDetails_assertModelProperties() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
    when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName("name")
        .withRegisteredNumber("registered number")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getPortalOrganisationId()),
        ApplicantDetailController.PRE_SELECTED_APPLICANT_ORGANISATION_PURPOSE
    ))
        .thenReturn(Optional.of(portalOrganisationUnit));

    mockMvc.perform(
            get(ReverseRouter.route(
                on(ApplicantDetailController.class).getUpdateApplicantDetails(nominationId)
            ))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/applicantdetails/applicantDetails"))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class).searchPortalOrganisations(null))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(ApplicantDetailController.class).updateApplicantDetails(nominationId, null, null))
        ))
        .andExpect(model().attribute(
            "preselectedItems",
            Map.of(
                String.valueOf(portalOrganisationUnit.id()),
                "%s (%s)".formatted(portalOrganisationUnit.name(), portalOrganisationUnit.registeredNumber().value())
            )
        ))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)),
                NominationTaskListController.PAGE_NAME
            )
        ))
        .andExpect(model().attribute("currentPage", ApplicantDetailController.PAGE_NAME));
  }

  @Test
  void getUpdateApplicantDetails_whenInvalidPreviouslySelectedItem_thenEmptyMap() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId("non numeric id");
    when(applicantDetailFormService.getForm(nominationDetail)).thenReturn(form);

    mockMvc.perform(
            get(ReverseRouter.route(
                on(ApplicantDetailController.class).getUpdateApplicantDetails(nominationId)
            ))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/applicantdetails/applicantDetails"))
        .andExpect(model().attribute("preselectedItems", Map.of()));

    verify(portalOrganisationUnitQueryService, never()).getOrganisationById(any(), any());
  }

  @Test
  void updateApplicantDetails_whenValidForm_assertRedirection() throws Exception {
    var form = ApplicantDetailTestUtil.getValidApplicantDetailForm();
    var applicationDetail = new ApplicantDetail(UUID.randomUUID());
    applicationDetail.setPortalOrganisationId(Integer.valueOf(form.getPortalOrganisationId()));
    applicationDetail.setApplicantReference(form.getApplicantReference());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);
    when(applicantDetailPersistenceService.createOrUpdateApplicantDetail(any(), eq(nominationDetail)))
        .thenReturn(applicationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(ApplicantDetailController.class).updateApplicantDetails(nominationId, null, null)
            ))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId))));

    verify(applicantDetailPersistenceService, times(1)).createOrUpdateApplicantDetail(any(), eq(nominationDetail));
    verify(nominationService, never()).startNomination();
  }

  @Test
  void updateApplicantDetails_whenInvalidForm_assertOk() throws Exception {
    var form = new ApplicantDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(applicantDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(ApplicantDetailController.class).updateApplicantDetails(nominationId, null, null)
            ))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk());

    verify(nominationService, never()).startNomination();
    verify(applicantDetailPersistenceService, never()).createOrUpdateApplicantDetail(any(), any());
  }
}