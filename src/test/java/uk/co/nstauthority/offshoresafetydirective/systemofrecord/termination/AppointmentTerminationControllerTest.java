package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = AppointmentTerminationController.class)
class AppointmentTerminationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  private static final AssetName ASSET_NAME = new AssetName("asset name");
  private static final List<AssetAppointmentPhase> ASSET_APPOINTMENT_PHASES =  List.of(new AssetAppointmentPhase("Development"));
  private static final String APPOINTED_OPERATOR_NAME = "appointed org name";
  private static final String RESPONSIBLE_FROM_DATE = "4 August 2023";
  private static final String CREATED_BY_DEEMED_APPOINTMENT = "Deemed appointment";

  @MockBean
  private AppointmentTerminationValidator appointmentTerminationValidator;

  @Autowired
  private AppointmentAccessService appointmentAccessService;

  @BeforeEach
  void setUp() {
    var asset = AssetTestUtil.builder()
        .withAssetName(ASSET_NAME.value())
        .build();
    var assetDto = AssetDto.fromAsset(asset);

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID.id())
        .withAsset(asset)
        .withAppointedPortalOperatorId(10)
        .withResponsibleFromDate(LocalDate.of(2023, 8, 4))
        .withCreatedByNominationId(20)
        .build();

    when(appointmentTerminationService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));

    when(appointmentTerminationService.getAssetName(assetDto))
        .thenReturn(ASSET_NAME);

    AppointmentDto appointmentDto = AppointmentDto.fromAppointment(appointment);

    when(appointmentTerminationService.getAppointedOperator(appointmentDto.appointedOperatorId()))
        .thenReturn(APPOINTED_OPERATOR_NAME);

    when(appointmentTerminationService.getAppointmentPhases(appointment, assetDto))
        .thenReturn(ASSET_APPOINTMENT_PHASES);

    when(appointmentTerminationService.getCreatedByDisplayString(appointmentDto))
        .thenReturn(CREATED_BY_DEEMED_APPOINTMENT);
  }

  @SecurityTest
  void testPermissions_onlyManageAppointmentPermitted() {
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    new HasPermissionSecurityTestUtil.SmokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)),
            status().isOk(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(APPOINTMENT_ID, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderTermination_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderTermination_whenAppointmentIsCurrent_andHasNotBeenTerminated_thenAssertOk() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
         .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void renderTermination_whenAppointmentIsNotCurrent_thenAssertForbidden() throws Exception {
    var endedAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withId(APPOINTMENT_ID.id())
        .build();
    var endAppointmentDto = AppointmentDto.fromAppointment(endedAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(endAppointmentDto));

    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTermination_whenAppointmentHasBeenTerminated_thenForbidden() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    when(appointmentTerminationService.hasBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderTermination_whenAppointmentNotFound_thenAssertNotFound() throws Exception {

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.empty());

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @EnumSource(PortalAssetType.class)
  void renderTermination_assertModelProperties(PortalAssetType portalAssetType) throws Exception {
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var currentAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withResponsibleFromDate(LocalDate.of(2023, 8, 4))
        .withId(APPOINTMENT_ID.id())
        .build();

    var assetDto = AssetDto.fromAsset(asset);
    var appointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    when(appointmentTerminationService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(currentAppointment));

    when(appointmentTerminationService.getAssetName(assetDto))
        .thenReturn(ASSET_NAME);

    when(appointmentTerminationService.getAppointedOperator(appointmentDto.appointedOperatorId()))
        .thenReturn("appointed org name");

    when(appointmentTerminationService.getAppointmentPhases(currentAppointment, assetDto))
        .thenReturn(ASSET_APPOINTMENT_PHASES);

    when(appointmentTerminationService.getCreatedByDisplayString(appointmentDto))
        .thenReturn("Deemed appointment");

    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    mockMvc.perform(get(
            ReverseRouter.route(on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/termination/terminateAppointment"))
        .andExpect(model().attribute("assetName", ASSET_NAME.value()))
        .andExpect(model().attribute("appointedOperator", APPOINTED_OPERATOR_NAME))
        .andExpect(model().attribute("responsibleFromDate", RESPONSIBLE_FROM_DATE))
        .andExpect(model().attribute("phases", ASSET_APPOINTMENT_PHASES))
        .andExpect(model().attribute("createdBy", CREATED_BY_DEEMED_APPOINTMENT))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(AppointmentTerminationController.class)
            .submitTermination(APPOINTMENT_ID, null, null, null))))
        .andExpect(model().attribute("timelineUrl", getExpectedRedirect(appointmentDto, portalAssetType)));
  }

  @SecurityTest
  void submitTermination_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(appointmentId, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitTermination_whenAppointmentIsNotCurrent_thenAssertForbidden() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(APPOINTMENT_ID, null, null, null)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitTermination_whenAppointmentIsCurrent_andHasNotBeenTerminated_thenAssertOk() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .willReturn(true);

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(APPOINTMENT_ID, null, null, null)))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void submitTermination_whenAppointmentHasBeenTerminated_thenForbidden() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentTerminationService.hasBeenTerminated(APPOINTMENT_ID))
        .willReturn(true);

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(APPOINTMENT_ID, null, null, null)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitTermination_whenHasError_thenOk() throws Exception {
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("object", "field", "message"));
      return invocation;
    }).when(appointmentTerminationValidator).validate(any(AppointmentTerminationForm.class), any(), any());

    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(APPOINTMENT_ID, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/termination/terminateAppointment"));
  }

  @ParameterizedTest
  @EnumSource(PortalAssetType.class)
  void submitTermination_verifySubmitRedirect_andVerfiyMethodCalls(PortalAssetType portalAssetType) throws Exception {

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(APPOINTMENT_ID.id())
        .build();

    var assetDto = AssetDto.fromAsset(asset);
    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    when(appointmentTerminationService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);


    var assetName = "asset name";
    when(appointmentTerminationService.getAssetName(assetDto))
        .thenReturn(new AssetName(assetName));

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Terminated appointment for %s".formatted(assetName))
        .build();

    mockMvc.perform(post(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).submitTermination(APPOINTMENT_ID, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(getExpectedRedirect(appointmentDto, portalAssetType)))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(appointmentTerminationService).terminateAppointment(
        eq(appointment),
        any(AppointmentTerminationForm.class)
    );
  }

  private String getExpectedRedirect(AppointmentDto appointmentDto, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderInstallationTimeline(appointmentDto.assetDto().portalAssetId()));
      case WELLBORE -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderWellboreTimeline(appointmentDto.assetDto().portalAssetId()));
      case SUBAREA -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderSubareaTimeline(appointmentDto.assetDto().portalAssetId()));
    };
  }
}