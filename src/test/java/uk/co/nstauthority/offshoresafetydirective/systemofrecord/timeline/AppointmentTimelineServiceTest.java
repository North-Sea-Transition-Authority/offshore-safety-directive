package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class AppointmentTimelineServiceTest {

  @Mock
  private PortalAssetNameService portalAssetNameService;

  @Mock
  private AssetAccessService assetAccessService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

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

  @InjectMocks
  private AppointmentTimelineService appointmentTimelineService;

  @Test
  void getAppointmentHistoryForPortalAsset_whenNotInPortalOrSystemOfRecord_thenEmptyOptional() {

    var portalAssetId = new PortalAssetId("something not in sor or portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isEmpty();
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAssetFromPortal_thenAssetNameIsPortalName() {

    var portalAssetId = new PortalAssetId("something in portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.of(new AssetName("from portal")));

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get())
        .extracting(assetAppointmentHistory -> assetAppointmentHistory.assetName().value())
        .isEqualTo("from portal");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAssetFromSystemOfRecord_thenAssetNameCachedAssetName() {

    var portalAssetId = new PortalAssetId("something in sor and not from portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get())
        .extracting(AssetAppointmentHistory::assetName)
        .isEqualTo(assetInSystemOfRecord.assetName());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenNoAppointments_thenEmptyAppointmentViewList() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(Collections.emptyList());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).isEmpty();

    then(organisationUnitQueryService).shouldHaveNoInteractions();
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointment_thenPopulatedAppointmentViewList() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    // given an appointment with an operator known to the portal

    var appointedOperatorId = new PortalOrganisationUnitId(100);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    var expectedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(expectedAppointment));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the expected appointment is returned
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews())
        .extracting(
            AssetTimelineItemView::timelineEventType,
            AssetTimelineItemView::title,
            AssetTimelineItemView::createdInstant
        )
        .containsExactly(
            tuple(
                TimelineEventType.APPOINTMENT,
                appointedOperator.name(),
                expectedAppointment.appointmentCreatedDate()
            )
        );

    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    var timelineView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);
    assertThat(timelineView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("appointmentId", expectedAppointment.appointmentId())
        .containsEntry("appointmentFromDate", expectedAppointment.appointmentFromDate())
        .containsEntry("appointmentToDate", expectedAppointment.appointmentToDate())
        .containsEntry("assetDto", expectedAppointment.assetDto());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointmentButOperatorNotInPortal_thenUnknownOperatorName() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    // given an appointment with an operator not known to the portal

    var appointedOperatorId = new PortalOrganisationUnitId(-1);

    var expectedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(Collections.emptyList());

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(expectedAppointment));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the operator name will be a sensible default string
    assertThat(resultingAppointmentTimelineHistory).isPresent();

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);
    assertThat(timelineItemView.title()).isEqualTo("Unknown operator");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenMultipleAppointments_thenOrderedByDescendingStartDate() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

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

    // and the timelineItemViews are returned out of order
    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(earliestAppointmentByStartDate, latestAppointmentByStartDate));

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the appointments are sorted by start date descending
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews())
        .extracting(view -> view.assetTimelineModelProperties().getModelProperties().get("appointmentId"))
        .containsExactly(
            latestAppointmentByStartDate.appointmentId(),
            earliestAppointmentByStartDate.appointmentId()
        );

  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenMultipleAppointmentsWithSameStartDate_thenOrderedByDescendingCreationDatetime() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

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

    // and the timelineItemViews are returned out of order
    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(earliestAppointmentByCreationTime, latestAppointmentByCreationTime));

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the timelineItemViews are sorted by creation date descending
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews())
        .map(assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties().getModelProperties())
        .map(stringObjectMap -> (AppointmentId) stringObjectMap.get("appointmentId"))
        .containsExactly(
            latestAppointmentByCreationTime.appointmentId(),
            earliestAppointmentByCreationTime.appointmentId()
        );
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenNoPhasesForAppointment_thenEmptyPhaseListInView() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(Collections.emptyMap());

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var appointment = AppointmentDtoTestUtil.builder().build();

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases).isEmpty();
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenInstallationAndKnownPhasesForAppointment_thenPopulatedPhaseListInView() {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(expectedInstallationPhase.getScreenDisplayText());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenInstallationAndUnknownPhasesForAppointment_thenUnknownPhasesIgnored() {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    @SuppressWarnings("unchecked")
    var phases = (List<AssetAppointmentPhase>) timelineItemView.assetTimelineModelProperties()
        .getModelProperties()
        .get("phases");

    assertThat(phases)
        .extracting(AssetAppointmentPhase::value)
        .containsExactly(knownInstallationPhase.getScreenDisplayText());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenInstallationMultiplePhasesForAppointment_thenOrderByPhaseDisplayOrder() {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

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
  void getAppointmentHistoryForPortalAsset_whenWellboreAndKnownPhasesForAppointment_thenPopulatedPhaseListInView(
      PortalAssetType portalAssetType
  ) {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

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
  void getAppointmentHistoryForPortalAsset_whenWellAndUnknownPhasesForAppointment_thenUnknownPhasesIgnored(
      PortalAssetType portalAssetType
  ) {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

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
  void getAppointmentHistoryForPortalAsset_whenWellMultiplePhasesForAppointment_thenOrderByPhaseDisplayOrder(
      PortalAssetType portalAssetType
  ) {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    // then only the known phase is returned
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);
    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

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
  void getAppointmentHistoryForPortalAsset_whenDeemedAppointment_thenCreatedByReferenceIsDeemed() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var deemedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.DEEMED)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(deemedAppointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", "Deemed appointment");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointmentFromLegacyNomination_andNoReference_thenCreatedByIsOffline() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var offlineAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withLegacyNominationReference(null)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(offlineAppointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", "Offline nomination");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointmentFromLegacyNomination_thenCreatedByReferenceIsLegacyReference() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var legacyAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withLegacyNominationReference("legacy nomination reference")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(legacyAppointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", legacyAppointment.legacyNominationReference());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointmentFromNomination_thenCreatedByReferenceIsNominationReference() {

    var portalAssetId = new PortalAssetId("something from system of record");

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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(nominatedAppointment));

    given(nominationAccessService.getNomination(nominationDto.nominationId()))
        .willReturn(Optional.of(nominationDto));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", nominationDto.nominationReference());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointmentFromUnknownNomination_thenCreatedByReferenceIsUnknown() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var unknownNominationId = new NominationId(-1);

    var unknownNominationAppointment = AppointmentDtoTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withLegacyNominationReference(null)
        .withNominationId(unknownNominationId)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(unknownNominationAppointment));

    given(nominationAccessService.getNomination(unknownNominationId))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", "Unknown");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenNoNominationId_thenNominationUrlIsNull() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var noNominationIdAppointment = AppointmentDtoTestUtil.builder()
        .withNominationId(null)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(noNominationIdAppointment));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenUserNotLoggedIn_thenNominationUrlIsNull() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenUserLoggedButNoPermissionOnNomination_thenNominationUrlIsNull() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS))
    )
        .willReturn(false);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenUserLoggedAndHasPermissionOnNomination_thenNominationUrlIsNotNull() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(permissionService.hasPermission(
        loggedInUser,
        Set.of(RolePermission.VIEW_NOMINATIONS, RolePermission.MANAGE_NOMINATIONS))
    )
        .willReturn(true);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "nominationUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(appointmentDto.nominationId(), null))
        );
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenUserLoggedAndHasPermissionToManageAppointments_thenCanManageAppointment() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentToDate(new AppointmentToDate(null))
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(userDetailService.isUserLoggedIn())
        .willReturn(true);

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

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

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
  void getAppointmentHistoryForPortalAsset_whenUserLoggedAndDoesNotHavePermissionToManageAppointments_thenCannotManageAppointment() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentToDate(new AppointmentToDate(LocalDate.now()))
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(userDetailService.isUserLoggedIn())
        .willReturn(true);

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

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("updateUrl")
        .doesNotContainKey("terminateUrl");
  }

  @ParameterizedTest
  @EnumSource(AppointmentType.class)
  void getAppointmentHistoryForPortalAssset_whenUnauthenticated_thenNoDeemedLetter(AppointmentType appointmentType) {
    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentType(appointmentType)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    given(userDetailService.isUserLoggedIn())
        .willReturn(false);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("deemedLetter");
  }

  @Test
  void getAppointmentHistoryForPortalAssset_whenAuthenticated_andDeemed_thenHasDeemedLetter() {
    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentType(AppointmentType.DEEMED)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    given(userDetailService.getUserDetail())
        .willReturn(ServiceUserDetailTestUtil.Builder().build());

    given(userDetailService.isUserLoggedIn())
        .willReturn(true);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

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
    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .withAppointmentType(appointmentType)
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    given(userDetailService.getUserDetail())
        .willReturn(ServiceUserDetailTestUtil.Builder().build());

    given(userDetailService.isUserLoggedIn())
        .willReturn(true);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("deemedLetter");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenUnauthenticated_thenCannotManageAppointment() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withNominationId(new NominationId(100))
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(appointmentDto));

    given(userDetailService.getUserDetail())
        .willReturn(null);

    given(userDetailService.isUserLoggedIn())
        .willReturn(false);

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistory.get().timelineItemViews().get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("updateUrl");
  }
}