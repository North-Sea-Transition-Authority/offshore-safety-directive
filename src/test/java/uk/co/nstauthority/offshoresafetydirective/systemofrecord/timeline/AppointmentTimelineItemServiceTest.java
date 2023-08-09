package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionHistoryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class AppointmentTimelineItemServiceTest {

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Mock
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private NominationAccessService nominationAccessService;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @Mock
  private AppointmentCorrectionService appointmentCorrectionService;

  @InjectMocks
  private AppointmentTimelineItemService appointmentTimelineItemService;

  @Test
  void getTimelineItemViews_whenAppointment_thenPopulatedAppointmentViewList() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    // given an appointment with an operator known to the portal

    var appointedOperatorId = new PortalOrganisationUnitId(100);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    var expectedAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // Treat user as not logged in to skip overhead in code we don't need to check
    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    // when we request the timeline history
    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(expectedAppointmentDto),
        assetInSystemOfRecord
    );

    // then the expected appointment is returned
    assertThat(resultingAppointmentTimelineHistoryItems)
        .extracting(
            AssetTimelineItemView::timelineEventType,
            AssetTimelineItemView::title,
            AssetTimelineItemView::createdInstant
        )
        .containsExactly(
            tuple(
                TimelineEventType.APPOINTMENT,
                appointedOperator.name(),
                expectedAppointmentDto.appointmentCreatedDate()
            )
        );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    var timelineView = resultingAppointmentTimelineHistoryItems.get(0);
    assertThat(timelineView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("appointmentId", expectedAppointmentDto.appointmentId())
        .containsEntry("appointmentFromDate", expectedAppointmentDto.appointmentFromDate())
        .containsEntry("appointmentToDate", expectedAppointmentDto.appointmentToDate())
        .containsEntry("assetDto", expectedAppointmentDto.assetDto());
  }

  @Test
  void getTimelineItemViews_whenAppointmentButOperatorNotInPortal_thenUnknownOperatorName() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    // given an appointment with an operator not known to the portal
    var appointedOperatorId = new PortalOrganisationUnitId(-1);

    var expectedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(Collections.emptyList());

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    // when we request the timeline history
    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(expectedAppointment),
        assetInSystemOfRecord
    );

    // then the operator name will be a sensible default string
    assertThat(resultingAppointmentTimelineHistoryItems)
        .extracting(AssetTimelineItemView::title)
        .containsExactly("Unknown operator");
  }

  @Test
  void getTimelineItemViews_whenMultipleAppointments_thenOrderedByDescendingStartDate() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(100);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given multiple timelineItemViews on different start dates

    var earliestAppointmentByStartDate = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.of(2022, 1, 1))
        .withAppointmentToDate(LocalDate.of(2022, 12, 1))
        .withAppointedOperatorId(appointedOperatorId.id())
        .withAppointmentId(UUID.randomUUID())
        .build();

    var latestAppointmentByStartDate = AppointmentDtoTestUtil.builder()
        .withAppointmentFromDate(LocalDate.of(2023, 1, 1))
        .withAppointmentToDate(LocalDate.of(2023, 2, 1))
        .withAppointedOperatorId(appointedOperatorId.id())
        .withAppointmentId(UUID.randomUUID())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    // when we request the timeline history
    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(earliestAppointmentByStartDate, latestAppointmentByStartDate),
        assetInSystemOfRecord
    );

    // then the appointments are sorted by start date descending
    assertThat(resultingAppointmentTimelineHistoryItems)
        .extracting(view -> view.assetTimelineModelProperties().getModelProperties().get("appointmentId"))
        .containsExactly(
            latestAppointmentByStartDate.appointmentId(),
            earliestAppointmentByStartDate.appointmentId()
        );

  }

  @Test
  void getTimelineItemViews_whenMultipleAppointmentsWithSameStartDate_thenOrderedByDescendingCreationDatetime() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    var appointedOperatorId = new PortalOrganisationUnitId(100);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given multiple timelineItemViews with the same start date

    var earliestAppointmentByCreationTime = AppointmentDtoTestUtil.builder()
        .withAppointmentCreatedDatetime(Instant.now().minus(1, ChronoUnit.DAYS))
        .withAppointmentFromDate(LocalDate.of(2022, 1, 1))
        .withAppointedOperatorId(appointedOperatorId.id())
        .withAppointmentId(UUID.randomUUID())
        .build();

    var latestAppointmentByCreationTime = AppointmentDtoTestUtil.builder()
        .withAppointmentCreatedDatetime(Instant.now())
        .withAppointmentFromDate(LocalDate.of(2022, 1, 1))
        .withAppointedOperatorId(appointedOperatorId.id())
        .withAppointmentId(UUID.randomUUID())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    // when we request the timeline history
    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(earliestAppointmentByCreationTime, latestAppointmentByCreationTime),
        assetInSystemOfRecord
    );

    // then the timelineItemViews are sorted by creation date descending
    assertThat(resultingAppointmentTimelineHistoryItems)
        .map(assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties().getModelProperties())
        .map(stringObjectMap -> (AppointmentId) stringObjectMap.get("appointmentId"))
        .containsExactly(
            latestAppointmentByCreationTime.appointmentId(),
            earliestAppointmentByCreationTime.appointmentId()
        );
  }

  @Test
  void getTimelineItemViews_whenNoPhasesForAppointment_thenEmptyPhaseListInView() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(Collections.emptyMap());

    var appointment = AppointmentDtoTestUtil.builder().build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases).isEmpty();
  }

  @Test
  void getTimelineItemViews_whenInstallationAndKnownPhasesForAppointment_thenPopulatedPhaseListInView() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var appointment = AppointmentDtoTestUtil.builder().build();

    var expectedInstallationPhase = InstallationPhase.DECOMMISSIONING;

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                appointment.appointmentId(), List.of(new AssetAppointmentPhase(expectedInstallationPhase.name()))
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(expectedInstallationPhase.getScreenDisplayText());
  }

  @Test
  void getTimelineItemViews_whenInstallationAndUnknownPhasesForAppointment_thenUnknownPhasesIgnored() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var appointment = AppointmentDtoTestUtil.builder().build();

    var unknownInstallationPhase = "NOT AN INSTALLATION PHASE";
    var knownInstallationPhase = InstallationPhase.DECOMMISSIONING;

    // given a phase which exists and a phase which doesn't
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                appointment.appointmentId(),
                List.of(
                    new AssetAppointmentPhase(unknownInstallationPhase),
                    new AssetAppointmentPhase(knownInstallationPhase.name())
                )
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(knownInstallationPhase.getScreenDisplayText());
  }

  @Test
  void getTimelineItemViews_whenInstallationMultiplePhasesForAppointment_thenOrderByPhaseDisplayOrder() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var appointment = AppointmentDtoTestUtil.builder().build();

    var firstPhaseByDisplayOrder = InstallationPhase.DEVELOPMENT_DESIGN;
    var secondPhaseByDisplayOrder = InstallationPhase.DECOMMISSIONING;

    // given multiple phases
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                appointment.appointmentId(),
                List.of(
                    new AssetAppointmentPhase(secondPhaseByDisplayOrder.name()),
                    new AssetAppointmentPhase(firstPhaseByDisplayOrder.name())
                )
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(
            firstPhaseByDisplayOrder.getScreenDisplayText(),
            secondPhaseByDisplayOrder.getScreenDisplayText()
        );
  }

  // wellbores and subareas use the same well phases so test both scenarios together
  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, mode = EnumSource.Mode.INCLUDE, names = {"WELLBORE", "SUBAREA"})
  void getTimelineItemViews_whenWellboreAndKnownPhasesForAppointment_thenPopulatedPhaseListInView(
      PortalAssetType portalAssetType
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentDtoTestUtil.builder().build();

    var expectedWellPhase = WellPhase.DECOMMISSIONING;

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                appointment.appointmentId(), List.of(new AssetAppointmentPhase(expectedWellPhase.name()))
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(expectedWellPhase.getScreenDisplayText());
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, mode = EnumSource.Mode.INCLUDE, names = {"WELLBORE", "SUBAREA"})
  void getTimelineItemViews_whenWellAndUnknownPhasesForAppointment_thenUnknownPhasesIgnored(
      PortalAssetType portalAssetType
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentDtoTestUtil.builder().build();

    var unknownWellPhase = "NOT A WELL PHASE";
    var knownWellPhase = WellPhase.DECOMMISSIONING;

    // given a phase which exists and a phase which doesn't
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                appointment.appointmentId(),
                List.of(
                    new AssetAppointmentPhase(unknownWellPhase),
                    new AssetAppointmentPhase(knownWellPhase.name())
                )
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(knownWellPhase.getScreenDisplayText());
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, mode = EnumSource.Mode.INCLUDE, names = {"WELLBORE", "SUBAREA"})
  void getTimelineItemViews_whenWellMultiplePhasesForAppointment_thenOrderByPhaseDisplayOrder(
      PortalAssetType portalAssetType
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentDtoTestUtil.builder().build();

    var firstPhaseByDisplayOrder = WellPhase.EXPLORATION_AND_APPRAISAL;
    var secondPhaseByDisplayOrder = WellPhase.DECOMMISSIONING;

    // given multiple phases
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                appointment.appointmentId(),
                List.of(
                    new AssetAppointmentPhase(secondPhaseByDisplayOrder.name()),
                    new AssetAppointmentPhase(firstPhaseByDisplayOrder.name())
                )
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(
            firstPhaseByDisplayOrder.getScreenDisplayText(),
            secondPhaseByDisplayOrder.getScreenDisplayText()
        );
  }

  @Test
  void getTimelineItemViews_whenDeemedAppointment_thenCreatedByReferenceIsDeemed() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var deemedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.DEEMED)
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(deemedAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", "Deemed appointment");
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromLegacyNomination_andNoReference_thenCreatedByIsOffline() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var offlineAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withLegacyNominationReference(null)
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(offlineAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", "Offline nomination");
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromLegacyNomination_thenCreatedByReferenceIsLegacyReference() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var legacyAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withLegacyNominationReference("legacy nomination reference")
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(legacyAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", legacyAppointment.legacyNominationReference());
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromNomination_thenCreatedByReferenceIsNominationReference() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var nominationDto = NominationDtoTestUtil.builder()
        .withNominationId(200)
        .withNominationReference("nomination reference")
        .build();

    var nominatedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withLegacyNominationReference(null)
        .withNominationId(nominationDto.nominationId())
        .build();

    given(nominationAccessService.getNomination(nominationDto.nominationId()))
        .willReturn(Optional.of(nominationDto));

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(nominatedAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", nominationDto.nominationReference());
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromUnknownNomination_thenCreatedByReferenceIsUnknown() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var unknownNominationId = new NominationId(-1);

    var unknownNominationAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withLegacyNominationReference(null)
        .withNominationId(unknownNominationId)
        .build();

    given(nominationAccessService.getNomination(unknownNominationId))
        .willReturn(Optional.empty());

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(unknownNominationAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", "Unknown");
  }

  @Test
  void getTimelineItemViews_whenNoNominationId_thenNominationUrlIsNull() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var noNominationIdAppointment = AppointmentDtoTestUtil.builder()
        .withNominationId(null)
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(noNominationIdAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @Test
  void getTimelineItemViews_whenUserNotLoggedIn_thenNominationUrlIsNull() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @Test
  void getTimelineItemViews_whenUserLoggedButNoPermissionOnNomination_thenNominationUrlIsNull() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.MANAGE_APPOINTMENTS))
    )
        .willReturn(false);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @Test
  void getTimelineItemViews_whenUserLoggedAndHasPermissionOnNomination_thenNominationUrlIsNotNull() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.MANAGE_APPOINTMENTS))
    )
        .willReturn(false);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS))
    )
        .willReturn(true);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "nominationUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(appointmentDto.nominationId(), null))
        );
  }

  @Test
  void getTimelineItemViews_whenUserLoggedAndHasPermissionToManageAppointments_thenCanManageAppointment() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentToDate(new AppointmentToDate(null))
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS))
    )
        .willReturn(true);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.MANAGE_APPOINTMENTS)
    ))
        .willReturn(true);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "updateUrl",
            ReverseRouter.route(on(AppointmentCorrectionController.class)
                .renderCorrection(appointmentDto.appointmentId()))
        )
        .containsEntry(
            "terminateUrl",
            ReverseRouter.route(on(AppointmentTerminationController.class)
                .renderTermination(appointmentDto.appointmentId()))
        );
  }

  @Test
  void getTimelineItemViews_whenUserLoggedAndDoesNotHavePermissionToManageAppointments_thenCannotManageAppointment() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentToDate(new AppointmentToDate(LocalDate.now()))
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();
    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS))
    )
        .willReturn(true);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.MANAGE_APPOINTMENTS)
    ))
        .willReturn(false);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("updateUrl")
        .doesNotContainKey("terminateUrl");
  }

  @ParameterizedTest
  @EnumSource(AppointmentType.class)
  void getAppointmentHistoryForPortalAssset_whenUnauthenticated_thenNoDeemedLetter(AppointmentType appointmentType) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentType(appointmentType)
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("deemedLetter");
  }

  @Test
  void getAppointmentHistoryForPortalAssset_whenAuthenticated_andDeemed_thenHasDeemedLetter() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentType(AppointmentType.DEEMED)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(ServiceUserDetailTestUtil.Builder().build());

    given(userDetailService.isUserLoggedIn())
        .willReturn(true);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    var expectedSummaryView = new FileSummaryView(
        DeemedLetterDownloadController.getAsUploadedFileView(),
        ReverseRouter.route(on(DeemedLetterDownloadController.class).download())
    );

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("deemedLetter", expectedSummaryView);
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "DEEMED")
  void getAppointmentHistoryForPortalAssset_whenAuthenticated_andNotDeemed_thenNoDeemedLetter(
      AppointmentType appointmentType
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentType(appointmentType)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(ServiceUserDetailTestUtil.Builder().build());

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("deemedLetter");
  }

  @Test
  void getTimelineItemViews_whenUnauthenticated_thenCannotManageAppointment() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("updateUrl");
  }

  @Test
  void getTimelineItemViews_whenUserLoggedAndIsMemberOfRegulatorTeam() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();
    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.MANAGE_APPOINTMENTS))
    )
        .willReturn(false);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS))
    )
        .willReturn(true);

    given(regulatorTeamService.isMemberOfRegulatorTeam(loggedInUser))
        .willReturn(true);

    var appointmentCorrectionHistoryView = AppointmentCorrectionHistoryViewTestUtil.builder()
        .withAppointmentId(appointmentDto.appointmentId())
        .build();
    given(appointmentCorrectionService.getAppointmentCorrectionHistoryViews(List.of(appointmentDto.appointmentId())))
        .willReturn(List.of(appointmentCorrectionHistoryView));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointmentDto),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);
    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "corrections",
            List.of(appointmentCorrectionHistoryView)
        );
  }
}