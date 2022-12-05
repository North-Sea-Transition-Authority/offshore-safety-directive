package uk.co.nstauthority.offshoresafetydirective.nomination.well;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = WellSelectionSetupController.class)
class WellSelectionSetupControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private NominationDetail nominationDetail;

  @MockBean
  private WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @MockBean
  private WellSelectionSetupFormService wellSelectionSetupFormService;

  @MockBean
  private WellSelectionSetupValidationService wellSelectionSetupValidationService;
  
  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);
    
    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = WellSelectionSetupFormTestUtil.builder().build();
    when(wellSelectionSetupFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(wellSelectionSetupValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(WellSelectionSetupController.class)
                .saveWellSetup(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("wellSelectionType", WellSelectionType.SPECIFIC_WELLS.name())
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var form = WellSelectionSetupFormTestUtil.builder().build();
    when(wellSelectionSetupFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(wellSelectionSetupValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(WellSelectionSetupController.class)
                .saveWellSetup(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("wellSelectionType", WellSelectionType.SPECIFIC_WELLS.name())
        .test();
  }

  @Test
  void getWellSetup_assertModelAndView() throws Exception {

    var form = new WellSelectionSetupForm();
    when(wellSelectionSetupFormService.getForm(nominationDetail)).thenReturn(form);

    mockMvc.perform(
        get(ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/wellSelectionSetup"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null))
        ))
        .andExpect(model().attribute("wellSelectionTypes", DisplayableEnumOptionUtil.getDisplayableOptions(WellSelectionType.class)))
        .andExpect(model().attribute("pageTitle", WellSelectionSetupController.PAGE_NAME));
  }

  @Test
  void saveWellSetup_whenErrors_thenExpectStatusOk() throws Exception {
    var form = WellSelectionSetupFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default msg"));
    when(wellSelectionSetupValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk());

    verify(wellSelectionSetupPersistenceService, never()).createOrUpdateWellSelectionSetup(any(), any(NominationDetail.class));
  }

  @Test
  void saveWellSetup_whenAnsweredSpecificWells_thenExpectRedirection() throws Exception {
    var form = WellSelectionSetupFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(wellSelectionSetupValidationService.validate(any(), any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
                .param("wellSelectionType", WellSelectionType.SPECIFIC_WELLS.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominatedWellDetailController.class)
            .renderNominatedWellDetail(NOMINATION_ID))));

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetupForm.class);
    verify(wellSelectionSetupPersistenceService, times(1))
        .createOrUpdateWellSelectionSetup(wellSetupCaptor.capture(), eq(nominationDetail));

    var capturedForm = wellSetupCaptor.getValue();
    assertEquals(WellSelectionType.SPECIFIC_WELLS.name(), capturedForm.getWellSelectionType());
  }

  @Test
  void saveWellSetup_whenAndAnsweredLicenceBlockSubarea_thenExpectRedirection() throws Exception {
    var form = WellSelectionSetupFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(wellSelectionSetupValidationService.validate(any(), any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
                .param("wellSelectionType", WellSelectionType.LICENCE_BLOCK_SUBAREA.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominatedBlockSubareaController.class)
            .getLicenceBlockSubareas(NOMINATION_ID))));

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetupForm.class);
    verify(wellSelectionSetupPersistenceService, times(1))
        .createOrUpdateWellSelectionSetup(wellSetupCaptor.capture(), eq(nominationDetail));

    var capturedForm = wellSetupCaptor.getValue();
    assertEquals(WellSelectionType.LICENCE_BLOCK_SUBAREA.name(), capturedForm.getWellSelectionType());
  }

  @Test
  void saveWellSetup_whenAndAnsweredNo_thenExpectRedirection() throws Exception {
    var form = WellSelectionSetupFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(wellSelectionSetupValidationService.validate(any(), any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
                .param("wellSelectionType", WellSelectionType.NO_WELLS.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))));

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetupForm.class);
    verify(wellSelectionSetupPersistenceService, times(1)).createOrUpdateWellSelectionSetup(wellSetupCaptor.capture(),
        eq(nominationDetail));

    var capturedForm = wellSetupCaptor.getValue();
    assertEquals(WellSelectionType.NO_WELLS.name(), capturedForm.getWellSelectionType());
  }
}