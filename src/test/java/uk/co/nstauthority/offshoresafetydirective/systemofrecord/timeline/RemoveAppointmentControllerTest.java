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
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = RemoveAppointmentController.class)
class RemoveAppointmentControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final PortalAssetId PORTAL_ASSET_ID = new PortalAssetId("123");
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  @MockitoBean
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @MockitoBean
  private AppointmentPhasesService appointmentPhasesService;

  @MockitoBean
  private AppointmentTimelineItemService appointmentTimelineItemService;

  @MockitoBean
  private AppointmentService appointmentService;

  @BeforeEach
  void setUp() {
    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);
  }

  @SecurityTest
  void renderRemoveAppointment_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
        .renderRemoveAppointment(APPOINTMENT_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderRemoveAppointment_whenUserWithoutCorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    givenExtantAssetAndAppointment();

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
        .renderRemoveAppointment(APPOINTMENT_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void removeAppointment_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class)
        .removeAppointment(APPOINTMENT_ID, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeAppointment_whenUserWithoutCorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    givenExtantAssetAndAppointment();

    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class)
        .removeAppointment(APPOINTMENT_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(value = AssetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "EXTANT")
  void renderRemoveAppointment_whenNonExtantAssetStatus_verifyForbidden(AssetStatus nonExtantStatus) throws Exception {
    var asset = AssetTestUtil.builder()
        .withAssetStatus(nonExtantStatus)
        .build();

    var currentAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(APPOINTMENT_ID.id())
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID)).thenReturn(Optional.of(currentAppointment));

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
        .renderRemoveAppointment(APPOINTMENT_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
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

    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class)
        .removeAppointment(APPOINTMENT_ID, null)))
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

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId, RemoveAppointmentController.OPERATOR_NAME_PURPOSE))
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
        .andExpect(model().attribute("portalAssetType", appointmentDto.assetDto().portalAssetType().name()))
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

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId, RemoveAppointmentController.OPERATOR_NAME_PURPOSE))
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

    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId, RemoveAppointmentController.OPERATOR_NAME_PURPOSE))
        .thenReturn(Optional.empty());

    when(appointmentTimelineItemService.getTimelineItemViews(List.of(appointment), appointmentDto.assetDto()))
        .thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(RemoveAppointmentController.class)
        .renderRemoveAppointment(APPOINTMENT_ID)))
        .with(user(USER)))
        .andExpect(status().isInternalServerError());
  }

  @ParameterizedTest
  @EnumSource(value = AssetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "EXTANT")
  void removeAppointment_whenNonExtantAssetStatus_verifyForbidden(AssetStatus nonExtantStatus) throws Exception {
    var asset = AssetTestUtil.builder()
        .withAssetStatus(nonExtantStatus)
        .build();

    var currentAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(APPOINTMENT_ID.id())
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID)).thenReturn(Optional.of(currentAppointment));

    mockMvc.perform(post(ReverseRouter.route(on(RemoveAppointmentController.class)
        .removeAppointment(APPOINTMENT_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
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

  private void givenExtantAssetAndAppointment() {

    var asset = AssetTestUtil.builder()
        .withAssetStatus(AssetStatus.EXTANT)
        .build();

    var currentAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(APPOINTMENT_ID.id())
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID)).thenReturn(Optional.of(currentAppointment));
  }
}