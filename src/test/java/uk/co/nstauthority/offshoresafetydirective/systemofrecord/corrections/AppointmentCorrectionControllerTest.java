package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
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

    new HasPermissionSecurityTestUtil.SmokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId)),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderCorrection_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    mockMvc.perform(get(
        ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId))))
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
            ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderCorrection_whenNoAsset_verifyNotFound() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.empty());

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    mockMvc.perform(get(
            ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
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

    mockMvc.perform(get(
            ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("assetName", assetName));
  }

  @Test
  void renderCorrection() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.of(appointmentDto));

    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId()))
        .thenReturn(Optional.of(assetDto));

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(new AssetName(assetName)));

    mockMvc.perform(get(
            ReverseRouter.route(on(AppointmentCorrectionController.class).renderCorrection(appointmentId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/correction/correctAppointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName()));
  }

}