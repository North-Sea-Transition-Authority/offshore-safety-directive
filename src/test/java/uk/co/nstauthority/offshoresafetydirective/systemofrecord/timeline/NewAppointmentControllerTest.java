package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionValidator;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.ForwardApprovedAppointmentRestController;
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

  @MockBean
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));
  }

  @SecurityTest
  void smokeTestPermissions() {
    var portalAssetId = new PortalAssetId("123");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetId(portalAssetId.id())
        .withAssetId(UUID.randomUUID())
        .build();

    when(assetAccessService.getAsset(assetDto.assetId())).thenReturn(Optional.of(assetDto));

    when(portalAssetRetrievalService.getAssetName(eq(assetDto.portalAssetId()), any()))
        .thenReturn(Optional.of("asset name"));

    when(portalAssetRetrievalService.isExtantInPortal(eq(portalAssetId), any(PortalAssetType.class)))
        .thenReturn(true);

    when(assetPersistenceService.getOrCreateAsset(eq(portalAssetId), any()))
        .thenReturn(assetDto);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewAppointment(assetDto.assetId()))
        )
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewInstallationAppointment(portalAssetId)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewWellboreAppointment(portalAssetId)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withGetEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewSubareaAppointment(portalAssetId)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NewAppointmentController.class)
                .createNewAppointment(assetDto.assetId(), null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @ParameterizedTest
  @EnumSource(value = AssetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "EXTANT")
  void renderNewAppointment_whenNonExtantAssetStatus_verifyForbidden(AssetStatus nonExtantStatus) throws Exception {
    var assetId = new AssetId(UUID.randomUUID());
    var asset = AssetDtoTestUtil.builder()
        .withStatus(nonExtantStatus)
        .withAssetId(assetId.id())
        .build();

    when(assetAccessService.getAsset(assetId)).thenReturn(Optional.ofNullable(asset));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(NewAppointmentController.class).renderNewAppointment(assetId)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderNewAppointment_whenAssetNotFound_thenError() throws Exception {
    var assetId = new AssetId(UUID.randomUUID());

    when(assetAccessService.getAsset(assetId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(
            ReverseRouter.route(
                on(NewAppointmentController.class).renderNewAppointment(assetId)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void renderNewAppointment_assertModelProperties
      (
          PortalAssetType portalAssetType,
          Function<PortalAssetId, String> submitRouteGenerator,
          Function<PortalAssetId, String> backRouteGenerator
      ) throws Exception {
    var portalAssetId = new PortalAssetId("123");
    var assetId = new AssetId(UUID.randomUUID());
    var asset = AssetDtoTestUtil.builder()
        .withAssetId(assetId.id())
        .withPortalAssetId(portalAssetId.id())
        .withPortalAssetType(portalAssetType)
        .build();

    when(assetAccessService.getAsset(assetId))
        .thenReturn(Optional.ofNullable(asset));

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    var appointmentTypes = AppointmentType.getDisplayableOptions(portalAssetType);

    mockMvc.perform(get(ReverseRouter.route(on(NewAppointmentController.class).renderNewAppointment(assetId)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("pageTitle", "Add appointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", portalAssetType.getDisplayName()))
        .andExpect(model().attribute("assetTypeSentenceCaseDisplayName", portalAssetType.getSentenceCaseDisplayName()))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(on(NewAppointmentController.class).createNewAppointment(assetId, null, null, null))))
        .andExpect(model().attribute("cancelUrl", backRouteGenerator.apply(portalAssetId)))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null, OrganisationFilterType.ALL.name()))
        ))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attribute("appointmentTypes", appointmentTypes))
        .andExpect(model().attribute(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        ))
        .andExpect(model().attribute("forwardApprovedAppointmentRestUrl",
            RestApiUtil.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(null)))
        );
  }

  @ParameterizedTest
  @EnumSource(PortalAssetType.class)
  void renderAppointment_whenNoAssetNameFound_thenError(PortalAssetType portalAssetType) throws Exception {
    var portalAssetId = new PortalAssetId("123");

    var asset = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .withPortalAssetId(portalAssetId.id())
        .build();

    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(asset);

    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NewAppointmentController.class).renderNewAppointment(asset.assetId())))
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

    var asset = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .withPortalAssetId(portalAssetId.id())
        .build();

    when(assetPersistenceService.getOrCreateAsset(portalAssetId, portalAssetType))
        .thenReturn(asset);

    when(portalAssetRetrievalService.isExtantInPortal(eq(portalAssetId), any(PortalAssetType.class)))
        .thenReturn(true);

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    mockMvc.perform(get(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NewAppointmentController.class)
            .renderNewAppointment(asset.assetId()))));
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void renderAppointment_whenAssetNotExtantInPortal_thenNotFound(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {
    var portalAssetId = new PortalAssetId("123");

    when(portalAssetRetrievalService.isExtantInPortal(portalAssetId, portalAssetType))
        .thenReturn(false);

    mockMvc.perform(get(submitRouteGenerator.apply(portalAssetId))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenHasError_thenVerifyAttributes(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    when(assetAccessService.getAsset(assetDto.assetId()))
        .thenReturn(Optional.of(assetDto));

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), portalAssetType))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    var appointmentTypes = AppointmentType.getDisplayableOptions(portalAssetType);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class)
                    .createNewAppointment(assetDto.assetId(), null, null, null)
            ))
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
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(NewAppointmentController.class)
            .createNewAppointment(assetDto.assetId(), null, null, null))))
        .andExpect(model().attribute("cancelUrl", backRouteGenerator.apply(assetDto.portalAssetId())))
        .andExpect(model().attribute(
            "portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null, OrganisationFilterType.ALL.name()))
        ))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attribute("appointmentTypes", appointmentTypes))
        .andExpect(model().attribute(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        ))
        .andExpect(model().attribute(
            "forwardApprovedAppointmentRestUrl",
            RestApiUtil.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(null))
        ))
        .andExpect(model().attributeDoesNotExist("preselectedOperator"));
  }

  @Test
  void createAppointment_whenHasError_andHasOperatorSelected_verifyHasPreselectedOperatorAttribute() throws Exception {
    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getAsset(assetDto.assetId()))
        .thenReturn(Optional.of(assetDto));

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType()))
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
    when(portalOrganisationUnitQueryService.getOrganisationById(operatorId, NewAppointmentController.PRE_SELECTED_OPERATOR_NAME_PURPOSE))
        .thenReturn(Optional.of(portalOrganisationDto));

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetDto.assetId(), null, null, null)))
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
  void createAppointment_whenInvalidPreviouslySelectedItem_thenEmptyMap() throws Exception {
    var assetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getAsset(assetDto.assetId()))
        .thenReturn(Optional.of(assetDto));

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    var phaseMap = Map.of("key", "value");
    when(appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType()))
        .thenReturn(phaseMap);

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId("FISH")
        .build();

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetDto.assetId(), null, null, null)))
            .with(user(USER))
            .with(csrf())
            .flashAttr("form", form))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedOperator", Map.of()));

    verify(portalOrganisationUnitQueryService, never()).getOrganisationById(any(), any());
  }

  @ParameterizedTest
  @MethodSource("portalAssetTypeAndEndpointArguments")
  void createAppointment_whenValid_thenVerifyRedirect(
      PortalAssetType portalAssetType,
      Function<PortalAssetId, String> submitRouteGenerator,
      Function<PortalAssetId, String> backRouteGenerator
  ) throws Exception {

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetAccessService.getAsset(assetDto.assetId()))
        .thenReturn(Optional.of(assetDto));

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), portalAssetType))
        .thenReturn(Optional.of(assetName));

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Added appointment for %s".formatted(
            assetName
        ))
        .build();

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetDto.assetId(), null, null, null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(backRouteGenerator.apply(assetDto.portalAssetId())))
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

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    when(assetAccessService.getAsset(assetDto.assetId()))
        .thenReturn(Optional.of(assetDto));

    when(assetAccessService.getAsset(assetDto.portalAssetId(), portalAssetType))
        .thenReturn(Optional.empty());

    var assetName = "asset name";
    when(portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), portalAssetType))
        .thenReturn(Optional.of(assetName));

    var asset = AssetTestUtil.builder().build();
    when(assetPersistenceService.persistNominatedAssets(any()))
        .thenReturn(List.of(asset));

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetDto.assetId(), null, null, null)
            ))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(backRouteGenerator.apply(assetDto.portalAssetId())));

    verify(appointmentService).addManualAppointment(any(), eq(assetDto));
  }

  @Test
  void createAppointment_whenInstallationDtoNotFound_thenVerifyError() throws Exception {
    var portalAssetIdAsInt = 123;

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetId(String.valueOf(portalAssetIdAsInt))
        .build();

    when(assetAccessService.getAsset(assetDto.assetId()))
        .thenReturn(Optional.of(assetDto));

    when(portalAssetRetrievalService.getInstallation(new InstallationId(portalAssetIdAsInt)))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetDto.assetId(), null, null, null)))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(AssetTimelineController.class).renderInstallationTimeline(assetDto.portalAssetId()))));
  }

  @Test
  void createAppointment_whenForwardApprovedAndAppointmentSelected_whenFormErrors_thenAppointmentRepopulated() throws Exception {
    var portalAssetId = new PortalAssetId("123");
    var createdByAppointmentAssetId = new AssetId(UUID.randomUUID());
    var assetIdOfNewAppointment = new AssetId(UUID.randomUUID());

    var createdByAppointmentAsset = AssetTestUtil.builder()
        .withId(createdByAppointmentAssetId.id())
        .withPortalAssetId(portalAssetId.id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .withAssetName("asset name")
        .build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAsset(createdByAppointmentAsset)
        .build();

    var assetDtoOfNewAppointment = AssetDtoTestUtil.builder()
        .withAssetId(assetIdOfNewAppointment.id())
        .build();

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    when(assetAccessService.getAsset(assetIdOfNewAppointment))
        .thenReturn(Optional.of(assetDtoOfNewAppointment));

    var forwardApprovedAppointmentId = UUID.randomUUID();

    when(appointmentAccessService.getAppointment(new AppointmentId(forwardApprovedAppointmentId)))
        .thenReturn(Optional.ofNullable(createdByAppointment));

    var subareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(createdByAppointmentAsset.getPortalAssetId()),
        NewAppointmentController.PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
    ))
        .thenReturn(Optional.of(subareaDto));

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetIdOfNewAppointment, null, null, null)))
            .param("forwardApprovedAppointmentId", forwardApprovedAppointmentId.toString())
            .param("appointmentType", AppointmentType.FORWARD_APPROVED.name())
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "preSelectedForwardApprovedAppointment",
            Map.of(
                createdByAppointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                    .formatted(subareaDto.displayName(), DateUtil.formatLongDate(createdByAppointment.getResponsibleFromDate()))
            )));
  }

  @Test
  void createNewAppointment_whenForwardApprovedAppointmentType_andSubareaIsNotFound_thenCachedAssetName() throws Exception {
    var portalAssetId = new PortalAssetId("123");
    var createdByAppointmentAssetId = new AssetId(UUID.randomUUID());
    var assetIdOfNewAppointment = new AssetId(UUID.randomUUID());

    var createdByAsset = AssetTestUtil.builder()
        .withId(createdByAppointmentAssetId.id())
        .withPortalAssetId(portalAssetId.id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .withAssetName("asset name")
        .build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAsset(createdByAsset)
        .build();

    var assetDtoOfNewAppointment = AssetDtoTestUtil.builder()
        .withAssetId(assetIdOfNewAppointment.id())
        .build();

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error.message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(), any(), any());

    when(assetAccessService.getAsset(assetIdOfNewAppointment))
        .thenReturn(Optional.of(assetDtoOfNewAppointment));

    var forwardApprovedAppointmentId = UUID.randomUUID();

    when(appointmentAccessService.getAppointment(new AppointmentId(forwardApprovedAppointmentId)))
        .thenReturn(Optional.ofNullable(createdByAppointment));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(createdByAsset.getPortalAssetId()),
        NewAppointmentController.PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
    ))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NewAppointmentController.class).createNewAppointment(assetIdOfNewAppointment, null, null, null)))
            .param("forwardApprovedAppointmentId", forwardApprovedAppointmentId.toString())
        .param("appointmentType", AppointmentType.FORWARD_APPROVED.name())
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "preSelectedForwardApprovedAppointment",
            Map.of(
                createdByAppointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                    .formatted(createdByAsset.getAssetName(), DateUtil.formatLongDate(createdByAppointment.getResponsibleFromDate()))
            )));
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