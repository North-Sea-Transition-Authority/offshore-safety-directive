package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AppointmentTimelineController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = AppointmentCorrectionController.class)
class AppointmentCorrectionControllerTest extends AbstractControllerTest {

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private AppointmentAccessService appointmentAccessService;

  @MockBean
  private AssetAccessService assetAccessService;

  @MockBean
  private PortalAssetNameService portalAssetNameService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private AppointmentCorrectionService appointmentCorrectionService;

  @MockBean
  private AppointmentCorrectionValidator appointmentCorrectionValidator;

  @SecurityTest
  void smokeTestPermissions() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(new AppointmentCorrectionForm());

    new HasPermissionSecurityTestUtil.SmokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).renderCorrection(appointmentId)),
            status().isOk(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderCorrection_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).renderCorrection(appointmentId))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderCorrection_whenNoAppointment_verifyNotFound() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.empty());

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderCorrection_whenNoAvailableAssetName_verifyCachedNameIsUsed() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var assetName = "asset name";
    var assetDto = AssetDtoTestUtil.builder()
        .withAssetName(assetName)
        .build();

    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.empty());

    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(new AppointmentCorrectionForm());

    mockMvc.perform(get(
            ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("assetName", assetName));
  }

  @Test
  void renderCorrection_whenNoOperatorInForm_thenPreSelectedOperatorNotPopulated() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .build();

    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var assetDto = AssetDtoTestUtil.builder().build();
    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(new AppointmentCorrectionForm());

    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var assetName = "asset name";

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(assetName)));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedOperator", Map.of()));
  }

  @Test
  void renderCorrection_whenOperatorInFormNotFound_thenPreSelectedOperatorNotPopulated() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());
    var operatorId = 200;

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .build();

    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(operatorId)
        .build();

    var assetDto = AssetDtoTestUtil.builder().build();
    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(form);

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId))
        .thenReturn(Optional.empty());

    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var assetName = "asset name";

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(assetName)));

    mockMvc.perform(
            get(
                ReverseRouter.route(
                    on(AppointmentCorrectionController.class).renderCorrection(appointmentId)
                )
            )
                .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedOperator", Map.of()));
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = "SUBAREA", mode = EnumSource.Mode.EXCLUDE)
  void renderCorrection_excludingSubarea(PortalAssetType portalAssetType) throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .withAssetDto(assetDto)
        .build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(assetName)));

    var expectedOrganisationId = Integer.valueOf(appointmentDto.appointedOperatorId().id());
    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(expectedOrganisationId))
        .thenReturn(Optional.of(organisationDto));

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(expectedOrganisationId)
        .build();

    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(form);

    var phaseMap = Map.of("PHASE_1", "phase 1");
    when(appointmentCorrectionService.getSelectablePhaseMap(assetDto))
        .thenReturn(phaseMap);

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/correction/correctAppointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName()))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null))))
        .andExpect(model().attribute("portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null))))
        .andExpect(model().attribute("preselectedOperator", Map.of(
            organisationDto.id().toString(),
            OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(organisationDto)
        )))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attributeDoesNotExist("phaseSelectionHint"));
  }

  @Test
  void renderCorrection_whenSubareaAsset() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .withAssetDto(assetDto)
        .build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(assetName)));

    var expectedOrganisationId = Integer.valueOf(appointmentDto.appointedOperatorId().id());
    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(expectedOrganisationId))
        .thenReturn(Optional.of(organisationDto));

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(expectedOrganisationId)
        .build();

    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(form);

    var phaseMap = Map.of("PHASE_1", "phase 1");
    when(appointmentCorrectionService.getSelectablePhaseMap(assetDto))
        .thenReturn(phaseMap);

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/correction/correctAppointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName()))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null))))
        .andExpect(model().attribute("portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null))))
        .andExpect(model().attribute("preselectedOperator", Map.of(
            organisationDto.id().toString(),
            OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(organisationDto)
        )))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attribute("phaseSelectionHint", "If decommissioning is required, another phase must be selected."));
  }

  @SecurityTest
  void submitCorrection_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void submitCorrection_whenNoAppointmentFound_thenNotFound() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void submitCorrection_whenHasError_thenOk() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(appointmentCorrectionService.getForm(appointmentDto))
        .thenReturn(new AppointmentCorrectionForm());

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("object", "field", "message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(AppointmentCorrectionForm.class), any(), any());

    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/correction/correctAppointment"));
  }

  @Test
  void submitCorrection_verifyCalls() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().is3xxRedirection());

    verify(appointmentCorrectionService).updateCorrection(
        eq(appointmentDto),
        any(AppointmentCorrectionForm.class)
    );
  }

  @ParameterizedTest
  @EnumSource(PortalAssetType.class)
  void submitCorrection_verifySubmitRedirect(PortalAssetType portalAssetType) throws Exception {

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .withAssetDto(assetDto)
        .build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(assetName)));

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Corrected appointment for %s".formatted(assetName))
        .build();

    var expectedRedirect = switch (portalAssetType) {
      case INSTALLATION -> ReverseRouter.route(on(AppointmentTimelineController.class)
          .renderInstallationAppointmentTimeline(appointmentDto.portalAssetId().toPortalAssetId()));
      case WELLBORE -> ReverseRouter.route(on(AppointmentTimelineController.class)
          .renderWellboreAppointmentTimeline(appointmentDto.portalAssetId().toPortalAssetId()));
      case SUBAREA -> ReverseRouter.route(on(AppointmentTimelineController.class)
          .renderSubareaAppointmentTimeline(appointmentDto.portalAssetId().toPortalAssetId()));
    };

    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirect))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }
}