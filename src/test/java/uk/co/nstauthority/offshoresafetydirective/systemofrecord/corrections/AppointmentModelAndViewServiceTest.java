package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.OrganisationFilterType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitRestController;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentModelAndViewService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.wellbore.WellboreAppointmentRestController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetTimelineController;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.MapEntryAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentModelAndViewServiceTest {

  private static final String PAGE_TITLE = "page title";

  private static final String SUBMIT_URL = "/submit";

  @Mock
  private PortalAssetRetrievalService portalAssetRetrievalService;

  @Mock
  private AppointmentCorrectionService appointmentCorrectionService;

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private AppointmentModelAndViewService appointmentModelAndViewService;

  @Test
  void getAppointmentModelAndView_assertBaseProperties() {
    var assetName = "portal asset name";
    var portalAssetType = PortalAssetType.WELLBORE;
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    when(portalAssetRetrievalService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType()))
        .thenReturn(Optional.of(assetName));

    var form = AppointmentCorrectionFormTestUtil.builder().build();

    var phases = DisplayableEnumOptionUtil.getDisplayableOptions(InstallationPhase.class);
    when(appointmentCorrectionService.getSelectablePhaseMap(assetDto.portalAssetType())).thenReturn(phases);

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getViewName()).isEqualTo("osd/systemofrecord/correction/correctAppointment");

    MapEntryAssert.thenAssertThat(resultingModelAndView.getModel())
        .hasKeyWithValue("pageTitle", PAGE_TITLE)
        .hasKeyWithValue("assetName", assetName)
        .hasKeyWithValue("assetTypeDisplayName", portalAssetType.getDisplayName())
        .hasKeyWithValue("assetTypeSentenceCaseDisplayName", portalAssetType.getSentenceCaseDisplayName())
        .hasKeyWithValue("portalOrganisationsRestUrl", RestApiUtil.route(on(PortalOrganisationUnitRestController.class)
            .searchAllPortalOrganisations(null, OrganisationFilterType.ALL.name())))
        .hasKeyWithValue("appointmentTypes", AppointmentType.getDisplayableOptions(portalAssetType))
        .hasKeyWithValue("phases", phases)
        .hasKeyWithValue("form", form)
        .hasKeyWithValue("nominationReferenceRestUrl",
            RestApiUtil.route(on(NominationReferenceRestController.class).searchPostSubmissionNominations(null))
        )
        .hasKeyWithValue(
            "forwardApprovedAppointmentRestUrl",
            RestApiUtil.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(null))
        )
        .hasKeyWithValue(
            "parentWellboreRestUrl",
            RestApiUtil.route(on(WellboreAppointmentRestController.class).searchWellboreAppointments(null))
        )
        .hasKeyWithValue("submitUrl", SUBMIT_URL)
        .hasAssertedAllKeysExcept(
            "preSelectedForwardApprovedAppointment",
            "preselectedNominationReference",
            "preselectedOperator",
            "preSelectedParentWellboreAppointment",
            "cancelUrl"
        );
  }

  @Test
  void getAppointmentModelAndView_whenOnlineNominationWithReference_thenPreSelectedNominationReference() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var reference = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withOnlineNominationReference(reference)
        .build();

    var nominationId = UUID.randomUUID();
    var nomination = NominationTestUtil.builder()
        .withId(nominationId)
        .withReference(reference)
        .build();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(new NominationId(UUID.fromString(reference))))
        .thenReturn(Optional.of(nominationDetail));

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preselectedNominationReference", Map.of(nominationId, reference));
  }

  @Test
  void getAppointmentModelAndView_whenOfflineNomination_thenPreSelectedNominationReference_andPreselectedForwardApprovedEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preselectedNominationReference", Map.of());
    assertThat(resultingModelAndView.getModel()).containsEntry("preSelectedForwardApprovedAppointment", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenNoNominationDetail_thenPreSelectedNominationReferenceEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var reference = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withOnlineNominationReference(reference)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(new NominationId(UUID.fromString(reference))))
        .thenReturn(Optional.empty());

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preselectedNominationReference", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenOnlineNomination_andNullReference_thenPreSelectedNominationReferenceEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withOnlineNominationReference(null)
        .build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preselectedNominationReference", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenNoForwardApprovedAppointment_thenPreselectedForwardApprovedEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var forwardApprovedAppointmentId = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.FORWARD_APPROVED)
        .withForwardApprovedAppointmentId(forwardApprovedAppointmentId)
        .build();

    when(appointmentAccessService.getAppointment(new AppointmentId(UUID.fromString(forwardApprovedAppointmentId))))
        .thenReturn(Optional.empty());

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preSelectedForwardApprovedAppointment", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenNoParentWellboreAppointment_thenPreselectedParentWellboreAppointmentEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var parentWellboreAppointmentId = UUID.randomUUID().toString();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.PARENT_WELLBORE)
        .withParentWellboreAppointmentId(parentWellboreAppointmentId)
        .build();

    when(appointmentAccessService.getAppointment(new AppointmentId(UUID.fromString(parentWellboreAppointmentId))))
        .thenReturn(Optional.empty());

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preSelectedParentWellboreAppointment", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenForwardApprovedAppointment_thenPreselectedForwardApprovedPopulatedWithPortalAssetName() {
    var assetDto = AssetDtoTestUtil.builder().build();

    var forwardApprovedAppointmentId = UUID.randomUUID();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.FORWARD_APPROVED)
        .withForwardApprovedAppointmentId(forwardApprovedAppointmentId.toString())
        .build();

    var startDate = LocalDate.now();
    var forwardApprovedAsset = AssetTestUtil.builder().build();

    var forwardApprovedAppointment = AppointmentTestUtil.builder()
        .withId(forwardApprovedAppointmentId)
        .withAsset(forwardApprovedAsset)
        .withResponsibleFromDate(startDate)
        .build();

    when(appointmentAccessService.getAppointment(new AppointmentId(forwardApprovedAppointmentId)))
        .thenReturn(Optional.of(forwardApprovedAppointment));

    var portalSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(forwardApprovedAsset.getPortalAssetId()),
        AppointmentModelAndViewService.PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
    )).thenReturn(Optional.of(portalSubarea));

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "preSelectedForwardApprovedAppointment",
            Map.of(
                forwardApprovedAppointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING.formatted(
                    portalSubarea.displayName(),
                    DateUtil.formatLongDate(startDate))
        ));
  }

  @Test
  void getAppointmentModelAndView_whenParentWellboreAppointment_thenPreselectedParentWellborePopulatedWithAssetName() {
    var assetDto = AssetDtoTestUtil.builder().build();

    var parentWellboreAppointmentId = UUID.randomUUID();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.PARENT_WELLBORE)
        .withParentWellboreAppointmentId(parentWellboreAppointmentId.toString())
        .build();

    var startDate = LocalDate.now();
    Integer wellId = 123;
    var parentWellboreAsset = AssetTestUtil.builder()
        .withPortalAssetId(wellId.toString())
        .build();

    var parentWellboreAppointment = AppointmentTestUtil.builder()
        .withId(parentWellboreAppointmentId)
        .withAsset(parentWellboreAsset)
        .withResponsibleFromDate(startDate)
        .build();

    when(appointmentAccessService.getAppointment(new AppointmentId(parentWellboreAppointmentId)))
        .thenReturn(Optional.of(parentWellboreAppointment));

    var wellDto = WellDtoTestUtil.builder().build();
    when(wellQueryService.getWell(
        new WellboreId(wellId),
        AppointmentModelAndViewService.PRE_SELECTED_PARENT_WELL_PURPOSE
    ))
        .thenReturn(Optional.of(wellDto));

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "preSelectedParentWellboreAppointment",
            Map.of(
                parentWellboreAppointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING.formatted(
                    wellDto.name(),
                    DateUtil.formatLongDate(startDate))
            ));
  }

  @Test
  void getAppointmentModelAndView_whenForwardApproved_andNullAppointmentId_thenPreselectedForwardApprovedEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.FORWARD_APPROVED)
        .withForwardApprovedAppointmentId(null)
        .build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preSelectedForwardApprovedAppointment", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenForwardApproved_andUsesCachedAssetName_thenPopulatedMapWithForwardApprovedAssetName() {
    var assetDto = AssetDtoTestUtil.builder().build();

    var forwardApprovedAppointmentId = UUID.randomUUID();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.FORWARD_APPROVED)
        .withForwardApprovedAppointmentId(forwardApprovedAppointmentId.toString())
        .build();

    var startDate = LocalDate.now();
    var forwardApprovedAsset = AssetTestUtil.builder().withAssetName("forward approved asset").build();

    var forwardApprovedAppointment = AppointmentTestUtil.builder()
        .withId(forwardApprovedAppointmentId)
        .withAsset(forwardApprovedAsset)
        .withResponsibleFromDate(startDate)
        .build();

    when(appointmentAccessService.getAppointment(new AppointmentId(forwardApprovedAppointmentId)))
        .thenReturn(Optional.of(forwardApprovedAppointment));

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(forwardApprovedAsset.getPortalAssetId()),
        AppointmentModelAndViewService.PRE_SELECTED_FORWARD_APPROVED_APPOINTMENT_PURPOSE
    )).thenReturn(Optional.empty());

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "preSelectedForwardApprovedAppointment",
            Map.of(
                forwardApprovedAppointment.getId(),
                ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING.formatted(
                    forwardApprovedAsset.getAssetName(),
                    DateUtil.formatLongDate(startDate))
        ));
  }

  @Test
  void getAppointmentModelAndView_whenOperatorIdIsNull_thenPreselectedOperatorEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId((String) null)
        .build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preselectedOperator", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenOperatorIdIsNotNumeric_thenPreselectedOperatorEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId("invalid")
        .build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel()).containsEntry("preselectedOperator", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenOperatorIdIsValid_thenPreselectedOperatorPopulated() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var organisationId = 123;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(organisationId)
        .build();

    var organisationDto = PortalOrganisationDtoTestUtil.builder()
        .withId(organisationId)
        .build();

    when(organisationUnitQueryService.getOrganisationById(organisationId, AppointmentModelAndViewService.PRE_SELECTED_OPERATOR_NAME_PURPOSE))
        .thenReturn(Optional.of(organisationDto));

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "preselectedOperator",
            Map.of(
                String.valueOf(organisationId),
                OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(organisationDto)
    ));
  }

  @Test
  void getAppointmentModelAndView_whenOperatorIdDoesNotExistInPortal_thenPreselectedOperatorEmpty() {
    var assetDto = AssetDtoTestUtil.builder().build();
    var organisationId = 123;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(organisationId)
        .build();

    when(organisationUnitQueryService.getOrganisationById(organisationId, AppointmentModelAndViewService.PRE_SELECTED_OPERATOR_NAME_PURPOSE))
        .thenReturn(Optional.empty());

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry("preselectedOperator", Map.of());
  }

  @Test
  void getAppointmentModelAndView_whenSubarea_assertTimelineRoute_andPhaseSelectionHint() {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    var form = AppointmentCorrectionFormTestUtil.builder().build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "cancelUrl",
            ReverseRouter.route(on(AssetTimelineController.class).renderSubareaTimeline(assetDto.portalAssetId()))
        );

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "phaseSelectionHint",
            "If decommissioning is required, another phase must be selected."
    );
  }

  @Test
  void getAppointmentModelAndView_whenInstallation_assertTimelineRoute_andNoPhaseSelectionHint() {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var form = AppointmentCorrectionFormTestUtil.builder().build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "cancelUrl",
            ReverseRouter.route(on(AssetTimelineController.class).renderInstallationTimeline(assetDto.portalAssetId()))
        );

    assertFalse(resultingModelAndView.getModel().containsKey("phaseSelectionHint"));
  }

  @Test
  void getAppointmentModelAndView_whenWellbore_assertTimelineRoute_andNoPhaseSelectionHint() {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();
    var form = AppointmentCorrectionFormTestUtil.builder().build();

    var resultingModelAndView = appointmentModelAndViewService.getAppointmentModelAndView(PAGE_TITLE, assetDto, form, SUBMIT_URL);

    assertThat(resultingModelAndView.getModel())
        .containsEntry(
            "cancelUrl",
            ReverseRouter.route(on(AssetTimelineController.class).renderWellboreTimeline(assetDto.portalAssetId()))
        );

    assertFalse(resultingModelAndView.getModel().containsKey("phaseSelectionHint"));
  }
}