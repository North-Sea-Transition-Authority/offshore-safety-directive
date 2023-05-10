package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.List;
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
import uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeAccidentRegulatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@IncludeAccidentRegulatorConfigurationProperties
@ContextConfiguration(classes = NominatedInstallationController.class)
class NominatedInstallationControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(1);

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private NominationDetail nominationDetail;

  @MockBean
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @MockBean
  private NominatedInstallationDetailFormService nominatedInstallationDetailFormService;

  @MockBean
  private InstallationQueryService installationQueryService;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .build();

    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominatedInstallationController.class)
                .saveNominatedInstallationDetail(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .build();

    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominatedInstallationController.class)
                .saveNominatedInstallationDetail(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }


  @Test
  void getNominatedInstallationDetail_assertModelProperties() throws Exception {

    var firstInstallationAlphabeticallyByName = InstallationDtoTestUtil.builder()
        .withId(1)
        .withName("A installation")
        .build();

    var lastInstallationAlphabeticallyByName = InstallationDtoTestUtil.builder()
        .withId(2)
        .withName("B installation")
        .build();

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(List.of(firstInstallationAlphabeticallyByName.id(), lastInstallationAlphabeticallyByName.id()))
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominatedInstallationDetailFormService.getForm(nominationDetail)).thenReturn(form);
    when(installationQueryService.getInstallationsByIdIn(
        List.of(firstInstallationAlphabeticallyByName.id(), lastInstallationAlphabeticallyByName.id()))
    )
        .thenReturn(List.of(lastInstallationAlphabeticallyByName, firstInstallationAlphabeticallyByName));

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"))
        .andExpect(model().attribute("pageTitle", NominatedInstallationController.PAGE_TITLE))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominatedInstallationController.class)
                .saveNominatedInstallationDetail(NOMINATION_ID, null, null))
        ))
        .andExpect(model().attribute(
            "installationPhases",
            DisplayableEnumOptionUtil.getDisplayableOptions(InstallationPhase.class)
        ))
        .andExpect(model().attribute(
            "alreadyAddedInstallations",
            List.of(
                new InstallationAddToListView(firstInstallationAlphabeticallyByName),
                new InstallationAddToListView(lastInstallationAlphabeticallyByName)
            )
        ))
        .andExpect(model().attribute(
            "installationsRestUrl",
            RestApiUtil.route(on(InstallationRestController.class)
                .searchInstallationsByNameAndType(null, NominatedInstallationController.PERMITTED_INSTALLATION_TYPES))
        ))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attributeExists("accidentRegulatorBranding"))
        .andExpect(model().attributeExists("org.springframework.validation.BindingResult.accidentRegulatorBranding"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(
        (AccidentRegulatorConfigurationProperties) modelAndView.getModel().get("accidentRegulatorBranding")
    )
        .hasNoNullFieldsOrProperties();


  }

  @Test
  void saveNominatedInstallationDetail_whenNoErrors_verifyServiceMethodCall() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageInstallationsController.class)
            .getManageInstallations(NOMINATION_ID))));

    verify(nominatedInstallationDetailPersistenceService, times(1))
        .createOrUpdateNominatedInstallationDetail(eq(nominationDetail), any(NominatedInstallationDetailForm.class));
  }

  @Test
  void saveNominatedInstallationDetail_whenErrors_assertStatusOk() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"));

    verify(nominatedInstallationDetailPersistenceService, never()).createOrUpdateNominatedInstallationDetail(any(), any());
  }
}