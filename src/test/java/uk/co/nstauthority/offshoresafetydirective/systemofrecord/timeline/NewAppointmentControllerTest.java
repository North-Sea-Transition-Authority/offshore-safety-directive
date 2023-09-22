package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidator;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.NominationReferenceRestController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NewAppointmentController.class)
class NewAppointmentControllerTest extends AbstractControllerTest {

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final Map<String, String> APPOINTMENT_TYPES = DisplayableEnumOptionUtil.getDisplayableOptions(
      AppointmentType.class);

  @MockBean
  private AssetAccessService assetAccessService;

  @MockBean
  private PortalAssetRetrievalService portalAssetRetrievalService;

  @MockBean
  private AppointmentCorrectionService appointmentCorrectionService;

  @MockBean
  private AppointmentService appointmentService;

  @MockBean
  private AssetPersistenceService assetPersistenceService;

  @MockBean
  private AppointmentCorrectionValidator appointmentCorrectionValidator;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));
  }

  @SecurityTest
  void smokeTestPermissions() {
    var portalAssetId = new PortalAssetId("123");
    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewInstallationAppointment(portalAssetId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewInstallationAppointment(portalAssetId, null, null)),
            status().isOk(),
            status().isForbidden()
        );
  }

  @Test
  void renderNewInstallationAppointment_whenNoAssetNameFound_thenError() throws Exception {
    var portalAssetId = new PortalAssetId("123");

    when(portalAssetRetrievalService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NewAppointmentController.class)
            .renderNewInstallationAppointment(portalAssetId)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderNewInstallationAppointment_verifyAttributes() throws Exception {
    var portalAssetId = new PortalAssetId("123");

    var installationName = "installation name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION))
        .thenReturn(Optional.of(installationName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(PortalAssetType.INSTALLATION))
        .thenReturn(phaseMap);

    mockMvc.perform(get(ReverseRouter.route(on(NewAppointmentController.class)
            .renderNewInstallationAppointment(portalAssetId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("pageTitle", "Add appointment"))
        .andExpect(model().attribute("assetName", installationName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.INSTALLATION.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeSentenceCaseDisplayName",
            PortalAssetType.INSTALLATION.getSentenceCaseDisplayName()
        ))
        .andExpect(model().attribute(
            "submitUrl",
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewInstallationAppointment(portalAssetId, null, null))
        ))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null))
        ))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attribute("appointmentTypes", APPOINTMENT_TYPES))
        .andExpect(model().attribute(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        ));
  }

  @Test
  void createNewInstallationAppointment_whenHasError_thenVerifyAttributes() throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));
    var portalAssetType = PortalAssetType.INSTALLATION;

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    var installationName = "installation name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION))
        .thenReturn(Optional.of(installationName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(PortalAssetType.INSTALLATION))
        .thenReturn(phaseMap);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(NewAppointmentController.class)
            .createNewInstallationAppointment(portalAssetId, null, null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("pageTitle", "Add appointment"))
        .andExpect(model().attribute("assetName", installationName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.INSTALLATION.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeSentenceCaseDisplayName",
            PortalAssetType.INSTALLATION.getSentenceCaseDisplayName()
        ))
        .andExpect(model().attribute(
            "submitUrl",
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewInstallationAppointment(portalAssetId, null, null))
        ))
        .andExpect(model().attribute(
            "cancelUrl",
            ReverseRouter.route(on(AssetTimelineController.class)
                .renderInstallationTimeline(portalAssetId))
        ))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null))
        ))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attribute("appointmentTypes", APPOINTMENT_TYPES))
        .andExpect(model().attribute(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        ))
        .andExpect(model().attributeDoesNotExist("preselectedOperator"));
  }

  @Test
  void createNewInstallationAppointment_whenHasError_andHasOperatorSelected_verifyHasPreselectedOperatorAttribute() throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));
    var portalAssetType = PortalAssetType.INSTALLATION;

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    var installationName = "installation name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION))
        .thenReturn(Optional.of(installationName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(PortalAssetType.INSTALLATION))
        .thenReturn(phaseMap);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    var operatorId = 222;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(operatorId)
        .build();

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .withId(operatorId)
        .build();
    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId))
        .thenReturn(Optional.of(portalOrganisationDto));

    mockMvc.perform(post(ReverseRouter.route(on(NewAppointmentController.class)
            .createNewInstallationAppointment(portalAssetId, null, null)))
            .with(user(USER))
            .with(csrf())
            .flashAttr("form", form))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "preselectedOperator",
            Map.of(
                String.valueOf(operatorId),
                portalOrganisationDto.displayName()
            )
        ));
  }

  @Test
  void createNewInstallationAppointment_whenValid_thenVerifyRedirect() throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));
    var portalAssetType = PortalAssetType.INSTALLATION;

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    var installationName = "installation name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION))
        .thenReturn(Optional.of(installationName));

    mockMvc.perform(post(ReverseRouter.route(on(NewAppointmentController.class)
            .createNewInstallationAppointment(portalAssetId, null, null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(AssetTimelineController.class)
                .renderInstallationTimeline(portalAssetId))
        ));

    verify(appointmentService).addManualAppointment(any(), eq(assetDto));
  }

  @Test
  void createNewInstallationAppointment_whenInstallationDtoNotFound_thenVerifyError() throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));
    var portalAssetType = PortalAssetType.INSTALLATION;

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    when(portalAssetRetrievalService.getInstallation(new InstallationId(portalAssetIdAsInt)))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(NewAppointmentController.class)
            .createNewInstallationAppointment(portalAssetId, null, null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}