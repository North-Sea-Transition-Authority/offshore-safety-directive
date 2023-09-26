package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.mockito.Mockito.mock;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = RemoveAppointmentController.class)
class RemoveAppointmentControllerTest extends AbstractControllerTest {

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final PortalAssetId PORTAL_ASSET_ID = new PortalAssetId("123");
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  @MockBean
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @MockBean
  private AppointmentPhasesService appointmentPhasesService;

  @MockBean
  private AppointmentTimelineItemService appointmentTimelineItemService;

  @MockBean
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @MockBean
  private AppointmentService appointmentService;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));
  }

  @SecurityTest
  void verifyUserCanOnlyAccessWithPermissions() {

    var asset = AssetTestUtil.builder().build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var assetTimelineView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();
    when(appointmentTimelineItemService.getTimelineItemViews(List.of(appointment), appointmentDto.assetDto()))
        .thenReturn(List.of(assetTimelineView));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(on(RemoveAppointmentController.class)
                .renderRemoveAppointment(APPOINTMENT_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(RemoveAppointmentController.class)
                .removeAppointment(APPOINTMENT_ID, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void verifyNoAccessIfTerminatedOnGet() throws Exception {

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.TERMINATED)
        .build();
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
            .renderRemoveAppointment(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void verifyNoAccessIfTerminatedOnPost() throws Exception {

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.TERMINATED)
        .build();
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class).removeAppointment(APPOINTMENT_ID, null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderRemoveAppointment_verifyAttributes() throws Exception {

    var operatorId = 222;

    var appointment = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(operatorId)
        .build();
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var operatorName = "operator name";
    var portalOrganisationDto = mock(PortalOrganisationDto.class);
    when(portalOrganisationDto.displayName()).thenReturn(operatorName);

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId))
        .thenReturn(Optional.of(portalOrganisationDto));

    var assetTimelineView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();
    when(appointmentTimelineItemService.getTimelineItemViews(List.of(appointment), appointmentDto.assetDto()))
        .thenReturn(List.of(assetTimelineView));

    var assetAppointmentPhase = new AssetAppointmentPhase("phase");
    when(assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment))
        .thenReturn(List.of(assetAppointmentPhase));

    when(appointmentPhasesService.getDisplayTextAppointmentPhases(
        appointmentDto.assetDto(),
        List.of(assetAppointmentPhase)
    ))
        .thenReturn(List.of(assetAppointmentPhase));

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
            .renderRemoveAppointment(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/removeAppointment"))
        .andExpect(model().attribute(
            "pageTitle",
            "Are you sure you want to remove this appointment for %s?".formatted(
                appointmentDto.assetDto().assetName().value()
            )))
        .andExpect(model().attribute("operatorName", operatorName))
        .andExpect(model().attribute("displayPhases", List.of(assetAppointmentPhase)))
        .andExpect(model().attribute("timelineItemView", assetTimelineView))
        .andExpect(model().attribute("assetName", appointmentDto.assetDto().assetName().value()));
  }

  @Test
  void renderRemoveAppointment_whenOrganisationNotFound_verifyUnknownAttributeValue() throws Exception {

    var operatorId = 222;

    var appointment = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(operatorId)
        .build();
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId))
        .thenReturn(Optional.empty());

    var assetTimelineView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();
    when(appointmentTimelineItemService.getTimelineItemViews(List.of(appointment), appointmentDto.assetDto()))
        .thenReturn(List.of(assetTimelineView));

    var assetAppointmentPhase = new AssetAppointmentPhase("phase");
    when(assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment))
        .thenReturn(List.of(assetAppointmentPhase));

    when(appointmentPhasesService.getDisplayTextAppointmentPhases(
        appointmentDto.assetDto(),
        List.of(assetAppointmentPhase)
    ))
        .thenReturn(List.of(assetAppointmentPhase));

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
            .renderRemoveAppointment(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("operatorName", "Unknown operator"));
  }

  @Test
  void renderRemoveAppointment_whenNoAppointment_thenNotFound() throws Exception {

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
            .renderRemoveAppointment(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderRemoveAppointment_whenNoAssetTimelineView_verifyInternalServerError() throws Exception {

    var operatorId = 222;

    var appointment = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(operatorId)
        .build();
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId))
        .thenReturn(Optional.empty());

    when(appointmentTimelineItemService.getTimelineItemViews(List.of(appointment), appointmentDto.assetDto()))
        .thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
            .renderRemoveAppointment(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void removeAppointment_verifyRemoved() throws Exception {

    var portalAssetType = PortalAssetType.INSTALLATION;
    var asset = AssetTestUtil.builder()
        .withPortalAssetId(PORTAL_ASSET_ID.id())
        .withPortalAssetType(portalAssetType)
        .build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Removed appointment for %s".formatted(
            asset.getAssetName()
        ))
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class)
            .removeAppointment(APPOINTMENT_ID, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            AssetTimelineController.determineRouteByPortalAssetType(
                PORTAL_ASSET_ID,
                portalAssetType
            )))
        .andExpect(notificationBanner(notificationBanner));

    verify(appointmentService).removeAppointment(appointment);
  }

  @Test
  void removeAppointment_whenAppointmentNotFound_verifyNotFound() throws Exception {
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class)
            .removeAppointment(APPOINTMENT_ID, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }
}