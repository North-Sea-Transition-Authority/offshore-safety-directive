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
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
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
  private static final Map<String, String> APPOINTMENT_TYPES =
      DisplayableEnumOptionUtil.getDisplayableOptions(AppointmentType.class);

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

    when(portalAssetRetrievalService.getAssetName(eq(portalAssetId), any()))
        .thenReturn(Optional.of("Asset name"));

    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetPersistenceService.getOrCreateAsset(eq(portalAssetId), any()))
        .thenReturn(assetDto);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewInstallationAppointment(portalAssetId))
        )
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewWellboreAppointment(portalAssetId))
        )
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewSubareaAppointment(portalAssetId))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewInstallationAppointment(portalAssetId, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewWellboreAppointment(portalAssetId, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewSubareaAppointment(portalAssetId, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void renderAppointment_whenNoAssetNameFound_thenError(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    var portalAssetId = new PortalAssetId("123");

    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void renderAppointment_verifyAttributes(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    var portalAssetId = new PortalAssetId("123");

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    mockMvc.perform(get(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("pageTitle", "Add appointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", portalAssetType.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeSentenceCaseDisplayName",
            portalAssetType.getSentenceCaseDisplayName()
        ))
        .andExpect(model().attribute("submitUrl", submitRouteGenerator.apply(portalAssetId)))
        .andExpect(model().attribute("cancelUrl", backRouteGenerator.apply(portalAssetId)))
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

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenHasError_thenVerifyAttributes(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    mockMvc.perform(post(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("pageTitle", "Add appointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", portalAssetType.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeSentenceCaseDisplayName",
            portalAssetType.getSentenceCaseDisplayName()
        ))
        .andExpect(model().attribute("submitUrl", submitRouteGenerator.apply(portalAssetId)))
        .andExpect(model().attribute("cancelUrl", backRouteGenerator.apply(portalAssetId)))
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

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenHasError_andHasOperatorSelected_verifyHasPreselectedOperatorAttribute(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
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

    mockMvc.perform(post(submitRouteGenerator.apply(portalAssetId))
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

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenValid_thenVerifyRedirect(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Added appointment for %s".formatted(
            assetName
        ))
        .build();

    mockMvc.perform(post(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(backRouteGenerator.apply(portalAssetId)))
        .andExpect(notificationBanner(notificationBanner));

    verify(appointmentService).addManualAppointment(any(), eq(assetDto));
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenAssetNotFound_thenVerifyError(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    when(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .thenReturn(Optional.empty());

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var asset = AssetTestUtil.builder().build();
    when(assetPersistenceService.persistNominatedAssets(any()))
        .thenReturn(List.of(asset));

    mockMvc.perform(post(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(backRouteGenerator.apply(portalAssetId)));

    verify(appointmentService).addManualAppointment(any(), eq(assetDto));
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenInstallationDtoNotFound_thenVerifyError(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    int portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(Integer.toString(portalAssetIdAsInt));

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(assetDto);

    when(portalAssetRetrievalService.getInstallation(new InstallationId(portalAssetIdAsInt)))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  private static Stream<Arguments> portalAssetTypeAndEndpointArguments() {
    Function<PortalAssetId, String> installationSubmitRoute = portalAssetId ->
        ReverseRouter.route(on(NewAppointmentController.class).renderNewInstallationAppointment(portalAssetId));
    Function<PortalAssetId, String> installationCancelRoute = portalAssetId ->
        ReverseRouter.route(on(AssetTimelineController.class).renderInstallationTimeline(portalAssetId));

    Function<PortalAssetId, String> wellboreSubmitRoute = portalAssetId ->
        ReverseRouter.route(on(NewAppointmentController.class).renderNewWellboreAppointment(portalAssetId));
    Function<PortalAssetId, String> wellboreCancelRoute = portalAssetId ->
        ReverseRouter.route(on(AssetTimelineController.class).renderWellboreTimeline(portalAssetId));

    Function<PortalAssetId, String> subareaSubmitRoute = portalAssetId ->
        ReverseRouter.route(on(NewAppointmentController.class).renderNewSubareaAppointment(portalAssetId));
    Function<PortalAssetId, String> subareaCancelRoute = portalAssetId ->
        ReverseRouter.route(on(AssetTimelineController.class).renderSubareaTimeline(portalAssetId));


    return Stream.of(
        Arguments.arguments(PortalAssetType.INSTALLATION, installationSubmitRoute, installationCancelRoute),
        Arguments.arguments(PortalAssetType.WELLBORE, wellboreSubmitRoute, wellboreCancelRoute),
        Arguments.arguments(PortalAssetType.SUBAREA, subareaSubmitRoute, subareaCancelRoute)
    );
  }
}