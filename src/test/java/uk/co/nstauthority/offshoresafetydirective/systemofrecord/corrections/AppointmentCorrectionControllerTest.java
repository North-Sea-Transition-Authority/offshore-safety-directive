package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentModelAndViewService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = AppointmentCorrectionController.class)
class AppointmentCorrectionControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private PortalAssetNameService portalAssetNameService;

  @MockBean
  private AppointmentCorrectionValidator appointmentCorrectionValidator;

  @BeforeEach
  void setup() {
    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);
  }

  @ParameterizedTest
  @EnumSource(value = AssetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "EXTANT")
  void renderCorrection_whenNonExtantAssetStatus_verifyForbidden(AssetStatus nonExtantStatus) throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var asset = AssetTestUtil.builder()
        .withAssetStatus(nonExtantStatus)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();

    when(appointmentTerminationService.hasBeenTerminated(appointmentId))
        .thenReturn(false);

    when(appointmentAccessService.getAppointment(appointmentId)).thenReturn(Optional.ofNullable(appointment));

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderCorrection_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderCorrection_whenIncorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(AppointmentTestUtil.builder().build()));

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderCorrection_whenAppointmentHasNotBeenTerminated_thenAssertOk() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder().build();
    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(new AppointmentCorrectionForm());

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderCorrection_whenAppointmentHasBeenTerminated_thenForbidden() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder().build();
    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(new AppointmentCorrectionForm());

    when(appointmentTerminationService.hasBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderCorrection_whenNoAppointment_verifyNotFound() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.findAppointmentDtoById(appointmentId))
        .thenReturn(Optional.empty());

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderCorrection_whenNoAvailableAssetName_verifyCachedNameIsUsed() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var assetName = "asset name";
    var asset = AssetTestUtil.builder()
        .withAssetName(assetName)
        .build();
    var assetDto = AssetDto.fromAsset(asset);

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();
    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.empty());

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(new AppointmentCorrectionForm());

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("assetName", assetName));
  }

  @Test
  void renderCorrection_whenNoOperatorInForm_thenPreSelectedOperatorNotPopulated() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var asset = AssetTestUtil.builder().build();
    var assetDto = AssetDto.fromAsset(asset);
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();
    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(new AppointmentCorrectionForm());

    var assetName = "asset name";

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedOperator", Map.of()));

  }

  @Test
  void renderCorrection_whenOperatorInFormNotFound_thenPreSelectedOperatorNotPopulated() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());
    var operatorId = 200;

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(operatorId)
        .build();

    var asset = AssetTestUtil.builder().build();
    var assetDto = AssetDto.fromAsset(asset);
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(portalOrganisationUnitQueryService.getOrganisationById(
        operatorId,
        AppointmentModelAndViewService.PRE_SELECTED_OPERATOR_NAME_PURPOSE
    ))
        .thenReturn(Optional.empty());

    var assetName = "asset name";

    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedOperator", Map.of()));
  }

  @Test
  void renderCorrection_whenInvalidPreviouslySelectedItem_thenEmptyMap() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId("FISH")
        .build();

    var asset = AssetTestUtil.builder().build();
    var assetDto = AssetDto.fromAsset(asset);
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedOperator", Map.of()));

    verify(portalOrganisationUnitQueryService, never()).getOrganisationById(any(), any());
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = "SUBAREA", mode = EnumSource.Mode.EXCLUDE)
  void renderCorrection_excludingSubarea(PortalAssetType portalAssetType) throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var assetDto = AssetDto.fromAsset(asset);
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();
    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    var expectedOrganisationId = Integer.valueOf(appointmentDto.appointedOperatorId().id());
    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        expectedOrganisationId,
        AppointmentModelAndViewService.PRE_SELECTED_OPERATOR_NAME_PURPOSE
    ))
        .thenReturn(Optional.of(organisationDto));

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(expectedOrganisationId)
        .build();

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    var phaseMap = Map.of("PHASE_1", "phase 1");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    var correctionHistoryView = AppointmentCorrectionHistoryViewTestUtil.builder().build();
    when(appointmentCorrectionService.getAppointmentCorrectionHistoryViews(appointment))
        .thenReturn(List.of(correctionHistoryView));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    var appointmentTypes = AppointmentType.getDisplayableOptions(portalAssetType);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
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
                .searchAllPortalOrganisations(null, OrganisationFilterType.ALL.name()))))
        .andExpect(model().attribute("preselectedOperator", Map.of(
            organisationDto.id().toString(),
            OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(organisationDto)
        )))
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
        .andExpect(model().attribute("correctionHistoryViews", List.of(correctionHistoryView)))
        .andExpect(model().attributeDoesNotExist("phaseSelectionHint"));
  }

  @ParameterizedTest
  @EnumSource(PortalAssetType.class)
  void renderCorrection_assertModelProperties(PortalAssetType portalAssetType) throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();

    var assetDto = AssetDto.fromAsset(asset);
    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    var expectedOrganisationId = Integer.valueOf(appointmentDto.appointedOperatorId().id());
    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        expectedOrganisationId,
        AppointmentModelAndViewService.PRE_SELECTED_OPERATOR_NAME_PURPOSE
    ))
        .thenReturn(Optional.of(organisationDto));

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(expectedOrganisationId)
        .build();

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    var phaseMap = Map.of("PHASE_1", "phase 1");
    when(appointmentCorrectionService.getSelectablePhaseMap(portalAssetType))
        .thenReturn(phaseMap);

    var correctionHistoryView = AppointmentCorrectionHistoryViewTestUtil.builder().build();
    when(appointmentCorrectionService.getAppointmentCorrectionHistoryViews(appointment))
        .thenReturn(List.of(correctionHistoryView));

    var appointmentTypes = AppointmentType.getDisplayableOptions(portalAssetType);

    var modelAndViewAssertions = mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/correction/correctAppointment"))
        .andExpect(model().attribute("pageTitle", "Update appointment"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName()))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(
                on(AppointmentCorrectionController.class).submitCorrection(appointmentId, null, null, null))))
        .andExpect(model().attribute("portalOrganisationsRestUrl",
            RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
                .searchAllPortalOrganisations(null, OrganisationFilterType.ALL.name()))))
        .andExpect(model().attribute("preselectedOperator", Map.of(
            organisationDto.id().toString(),
            OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(organisationDto)
        )))
        .andExpect(model().attribute("phases", phaseMap))
        .andExpect(model().attribute(
            "nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        ))
        .andExpect(model().attribute(
            "forwardApprovedAppointmentRestUrl",
            RestApiUtil.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(null))
        ))
        .andExpect(model().attribute("correctionHistoryViews", List.of(correctionHistoryView)))
        .andExpect(model().attribute("cancelUrl", getExpectedRedirect(appointmentDto, portalAssetType)))
        .andExpect(model().attribute("appointmentTypes", appointmentTypes))
        .andExpect(model().attribute("showCorrectionHistory", true));

    if (PortalAssetType.SUBAREA.equals(portalAssetType)) {
      modelAndViewAssertions.andExpect(
          model().attribute("phaseSelectionHint", "If decommissioning is required, another phase must be selected."));
    }

    assertThat(modelAndViewAssertions.andReturn().getModelAndView()).isNotNull();
  }

  @Test
  void renderCorrection_whenOnlineAppointmentType_andNoOnlineReference_thenPreselectedNominationIsEmpty() throws Exception {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var form = AppointmentCorrectionFormTestUtil.builder().build();

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedNominationReference", Map.of()));
  }

  @Test
  void renderCorrection_whenOnlineAppointmentType_andOnlineReferenceIsNotFound_thenEmptyPreselectedNomination() throws Exception {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var onlineReference = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withOnlineNominationReference(onlineReference)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(
        new NominationId(UUID.fromString(form.getOnlineNominationReference()))
    ))
        .thenReturn(Optional.empty());

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preselectedNominationReference", Map.of()));
  }

  @Test
  void renderCorrection_whenOnlineAppointmentType_andValidReference_thenHasPreselectedNomination() throws Exception {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var onlineReference = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withOnlineNominationReference(onlineReference)
        .build();

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetailOptional(
        new NominationId(UUID.fromString(form.getOnlineNominationReference()))
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "preselectedNominationReference",
            Map.of(nominationDetail.getNomination().getId(), nominationDetail.getNomination().getReference())
        ));
  }

  @Test
  void renderCorrection_whenForwardApprovedAppointmentType_andNoForwardApprovedAppointment_thenNoPreselectedForwardApproved()
      throws Exception {
    var appointmentType = AppointmentType.FORWARD_APPROVED;
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var form = AppointmentCorrectionFormTestUtil.builder().build();

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("preSelectedForwardApprovedAppointment", Map.of()));
  }


  @Test
  void renderCorrection_whenForwardApprovedAppointmentType_andSubareaIsNotFound_thenCachedAssetName()
      throws Exception {
    var appointmentType = AppointmentType.FORWARD_APPROVED;
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withId(appointmentId.id())
        .build();

    var forwardApprovedAppointmentId = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.FORWARD_APPROVED)
        .withForwardApprovedAppointmentId(forwardApprovedAppointmentId)
        .build();

    when(appointmentAccessService.getAppointment(
        new AppointmentId(UUID.fromString(form.getForwardApprovedAppointmentId()))))
        .thenReturn(Optional.of(appointment));

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(appointment.getAsset().getPortalAssetId()),
        AppointmentModelAndViewService.PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
    ))
        .thenReturn(Optional.empty());

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "preSelectedForwardApprovedAppointment",
            Map.of(
                appointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                    .formatted(appointment.getAsset().getAssetName(),
                        DateUtil.formatLongDate(appointment.getResponsibleFromDate()))
            )));
  }

  @Test
  void renderCorrection_whenForwardApprovedAppointmentType_andValidReference_thenHasPreselectedForwardApproval()
      throws Exception {
    var appointmentType = AppointmentType.FORWARD_APPROVED;
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withId(appointmentId.id())
        .build();

    var forwardApprovedAppointmentId = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.FORWARD_APPROVED)
        .withForwardApprovedAppointmentId(forwardApprovedAppointmentId)
        .build();

    when(appointmentAccessService.getAppointment(
        new AppointmentId(UUID.fromString(form.getForwardApprovedAppointmentId()))))
        .thenReturn(Optional.of(appointment));

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var subarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(appointment.getAsset().getPortalAssetId()),
        AppointmentModelAndViewService.PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
    ))
        .thenReturn(Optional.of(subarea));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(form);

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .renderCorrection(appointmentId)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "preSelectedForwardApprovedAppointment",
            Map.of(
                appointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                    .formatted(subarea.displayName(), DateUtil.formatLongDate(appointment.getResponsibleFromDate()))
            )));
  }

  @SecurityTest
  void submitCorrection_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitCorrection_whenIncorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(AppointmentTestUtil.builder().build()));

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitCorrection_whenAppointmentHasNotBeenTerminated_thenAssertOk() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void submitCorrection_whenAppointmentHasBeenTerminated_thenForbidden() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentTerminationService.hasBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(value = AssetStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "EXTANT")
  void submitCorrection_whenNonExtantAssetStatus_verifyForbidden(AssetStatus nonExtantStatus) throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var asset = AssetTestUtil.builder()
        .withAssetStatus(nonExtantStatus)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();

    when(appointmentTerminationService.hasBeenTerminated(appointmentId))
        .thenReturn(false);

    when(appointmentAccessService.getAppointment(appointmentId)).thenReturn(Optional.ofNullable(appointment));

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitCorrection_whenNoAppointmentFound_thenNotFound() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.empty());

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void submitCorrection_whenHasError_thenOk() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentCorrectionService.getForm(appointment))
        .thenReturn(new AppointmentCorrectionForm());

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("object", "field", "message"));
      return invocation;
    }).when(appointmentCorrectionValidator).validate(any(AppointmentCorrectionForm.class), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/correction/correctAppointment"));
  }

  @Test
  void submitCorrection_verifyCalls() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection());

    verify(appointmentCorrectionService).correctAppointment(
        eq(appointment),
        any(AppointmentCorrectionForm.class)
    );
  }

  @ParameterizedTest
  @EnumSource(PortalAssetType.class)
  void submitCorrection_verifySubmitRedirect(PortalAssetType portalAssetType) throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var asset = AssetTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withId(appointmentId.id())
        .build();

    var assetDto = AssetDto.fromAsset(asset);
    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    when(appointmentTerminationService.hasNotBeenTerminated(appointmentId))
        .thenReturn(true);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Updated appointment for %s".formatted(assetName))
        .build();

    var expectedRedirect = switch (portalAssetType) {
      case INSTALLATION -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderInstallationTimeline(appointmentDto.assetDto().portalAssetId()));
      case WELLBORE -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderWellboreTimeline(appointmentDto.assetDto().portalAssetId()));
      case SUBAREA -> ReverseRouter.route(on(AssetTimelineController.class)
          .renderSubareaTimeline(appointmentDto.assetDto().portalAssetId()));
    };

    mockMvc.perform(post(ReverseRouter.route(on(AppointmentCorrectionController.class)
        .submitCorrection(appointmentId, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(expectedRedirect))
        .andExpect(notificationBanner(expectedNotificationBanner));
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