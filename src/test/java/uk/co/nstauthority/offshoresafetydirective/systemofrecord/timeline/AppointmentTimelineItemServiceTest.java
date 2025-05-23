package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.consultee.NominationConsulteeViewController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionHistoryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.ForwardApprovedAppointmentRestService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class AppointmentTimelineItemServiceTest {

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Mock
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private NominationAccessService nominationAccessService;

  @Mock
  private AppointmentCorrectionService appointmentCorrectionService;

  @Mock
  private AppointmentPhasesService appointmentPhasesService;

  @Mock
  private SystemOfRecordConfigurationProperties systemOfRecordConfigurationProperties;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private PortalAssetNameService portalAssetNameService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private AppointmentTimelineItemService appointmentTimelineItemService;

  private ServiceUserDetail loggedInUser;

  @BeforeEach
  void setUp() {
    loggedInUser = ServiceUserDetailTestUtil.Builder().build();
  }

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

    var expectedAppointment = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(appointedOperatorId.id())
        .build();

    var expectedAppointmentDto = AppointmentDto.fromAppointment(expectedAppointment);

    given(organisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentTimelineItemService.APPOINTED_OPERATORS_PURPOSE
    ))
        .willReturn(List.of(appointedOperator));

    // Treat user as not logged in to skip overhead in code we don't need to check
    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    // when we request the timeline history
    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(expectedAppointment),
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

    var expectedAppointment = AppointmentTestUtil.builder()
        .withAppointedPortalOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(
        Set.of(appointedOperatorId),
        AppointmentTimelineItemService.APPOINTED_OPERATORS_PURPOSE
    ))
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
  void getTimelineItemViews_whenNoPhasesForAppointment_thenEmptyPhaseListInView() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(Collections.emptyMap());

    var appointment = AppointmentTestUtil.builder().build();

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

    var appointment = AppointmentTestUtil.builder().build();

    var expectedInstallationPhase = InstallationPhase.DECOMMISSIONING;

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                new AppointmentId(appointment.getId()),
                List.of(new AssetAppointmentPhase(expectedInstallationPhase.name()))
            )
        );

    given(appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetInSystemOfRecord,
        List.of(
            new AssetAppointmentPhase(expectedInstallationPhase.name())
        )))
        .willReturn(
            List.of(
                new AssetAppointmentPhase(expectedInstallationPhase.getScreenDisplayText())
            ));

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

    var appointment = AppointmentTestUtil.builder().build();

    var unknownInstallationPhase = "NOT AN INSTALLATION PHASE";
    var knownInstallationPhase = InstallationPhase.DECOMMISSIONING;

    // given a phase which exists and a phase which doesn't
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                new AppointmentId(appointment.getId()),
                List.of(
                    new AssetAppointmentPhase(unknownInstallationPhase),
                    new AssetAppointmentPhase(knownInstallationPhase.name())
                )
            )
        );

    given(appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetInSystemOfRecord,
        List.of(
            new AssetAppointmentPhase(unknownInstallationPhase),
            new AssetAppointmentPhase(knownInstallationPhase.name())
        )))
        .willReturn(
            List.of(
                new AssetAppointmentPhase(unknownInstallationPhase),
                new AssetAppointmentPhase(knownInstallationPhase.getScreenDisplayText())
            ));

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
        .containsExactly(unknownInstallationPhase, knownInstallationPhase.getScreenDisplayText());
  }

  @Test
  void getTimelineItemViews_whenInstallationMultiplePhasesForAppointment_thenOrderByPhaseDisplayOrder() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var appointment = AppointmentTestUtil.builder().build();

    var firstPhaseByDisplayOrder = InstallationPhase.DEVELOPMENT_DESIGN;
    var secondPhaseByDisplayOrder = InstallationPhase.DECOMMISSIONING;

    // given multiple phases
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                new AppointmentId(appointment.getId()),
                List.of(
                    new AssetAppointmentPhase(firstPhaseByDisplayOrder.name()),
                    new AssetAppointmentPhase(secondPhaseByDisplayOrder.name())
                )
            )
        );

    given(appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetInSystemOfRecord,
        List.of(
            new AssetAppointmentPhase(firstPhaseByDisplayOrder.name()),
            new AssetAppointmentPhase(secondPhaseByDisplayOrder.name())
        ))
    )
        .willReturn(
            List.of(
                new AssetAppointmentPhase(firstPhaseByDisplayOrder.getScreenDisplayText()),
                new AssetAppointmentPhase(secondPhaseByDisplayOrder.getScreenDisplayText())
            ));

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

    var appointment = AppointmentTestUtil.builder().build();

    var expectedWellPhase = WellPhase.DECOMMISSIONING;

    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                new AppointmentId(appointment.getId()), List.of(new AssetAppointmentPhase(expectedWellPhase.name()))
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    given(appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetInSystemOfRecord,
        List.of(
            new AssetAppointmentPhase(expectedWellPhase.name())
        ))
    )
        .willReturn(
            List.of(
                new AssetAppointmentPhase(expectedWellPhase.getScreenDisplayText())
            ));

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

    var appointment = AppointmentTestUtil.builder().build();

    var unknownWellPhase = "NOT A WELL PHASE";
    var knownWellPhase = WellPhase.DECOMMISSIONING;

    // given a phase which exists and a phase which doesn't
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                new AppointmentId(appointment.getId()),
                List.of(
                    new AssetAppointmentPhase(unknownWellPhase),
                    new AssetAppointmentPhase(knownWellPhase.name())
                )
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    given(appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetInSystemOfRecord,
        List.of(
            new AssetAppointmentPhase(unknownWellPhase),
            new AssetAppointmentPhase(knownWellPhase.name())
        )))
        .willReturn(List.of(new AssetAppointmentPhase(knownWellPhase.getScreenDisplayText())));

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

    var appointment = AppointmentTestUtil.builder().build();

    var firstPhaseByDisplayOrder = WellPhase.EXPLORATION_AND_APPRAISAL;
    var secondPhaseByDisplayOrder = WellPhase.DECOMMISSIONING;

    // given multiple phases
    given(assetAppointmentPhaseAccessService.getAppointmentPhases(assetInSystemOfRecord))
        .willReturn(
            Map.of(
                new AppointmentId(appointment.getId()),
                List.of(
                    new AssetAppointmentPhase(firstPhaseByDisplayOrder.name()),
                    new AssetAppointmentPhase(secondPhaseByDisplayOrder.name())
                )
            )
        );

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    given(appointmentPhasesService.getDisplayTextAppointmentPhases(
        assetInSystemOfRecord,
        List.of(
            new AssetAppointmentPhase(firstPhaseByDisplayOrder.name()),
            new AssetAppointmentPhase(secondPhaseByDisplayOrder.name())
        )))
        .willReturn(
            List.of(
                new AssetAppointmentPhase(firstPhaseByDisplayOrder.getScreenDisplayText()),
                new AssetAppointmentPhase(secondPhaseByDisplayOrder.getScreenDisplayText())
            ));

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

    var deemedAppointment = AppointmentTestUtil.builder()
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

    var offlineAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(null)
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
  void getTimelineItemViews_whenAppointmentFromLegacyNomination_andMemberOfRegulatorTeam_thenCreatedByReferenceIsLegacyReference() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var legacyNominationReference = "legacy nomination reference";

    var legacyAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(legacyNominationReference)
        .build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var manageAppointmentRole = new TeamRole();
    manageAppointmentRole.setTeam(regulatorTeam);
    manageAppointmentRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(manageAppointmentRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(legacyAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", legacyNominationReference)
        .containsEntry("offlineNominationDocumentUrl", systemOfRecordConfigurationProperties.offlineNominationDocumentUrl());
  }


  @ParameterizedTest
  @EnumSource(value = TeamType.class, mode = EnumSource.Mode.EXCLUDE, names = "REGULATOR")
  void getTimelineItemViews_whenOfflineNominationWithLegacyNominationReference_andNotRegulator_thenCreatedByReferenceHasNoUrl(
      TeamType invalidTeamType
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var legacyNominationReference = "legacy nomination reference";

    var legacyAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(legacyNominationReference)
        .build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var notRegulatorTeam = new Team();
    notRegulatorTeam.setTeamType(invalidTeamType);

    var organisationGroupRole = new TeamRole();
    organisationGroupRole.setTeam(notRegulatorTeam);
    organisationGroupRole.setRole(Role.NOMINATION_EDITOR);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(organisationGroupRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(legacyAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", legacyNominationReference)
        .doesNotContainKey("offlineNominationDocumentUrl");
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromLegacyNomination_andNoLegacyNominationProvided_thenCreatedByReferenceIsOfflineNomination() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var legacyAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(null)
        .build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var manageAppointmentRole = new TeamRole();
    manageAppointmentRole.setTeam(regulatorTeam);
    manageAppointmentRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(manageAppointmentRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(legacyAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText())
        .doesNotContainKey("offlineNominationDocumentUrl");
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromNomination_thenCreatedByReferenceIsNominationReference() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var nomination = NominationTestUtil.builder()
        .withId(UUID.randomUUID())
        .withReference("nomination reference")
        .build();

    var nominatedAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(null)
        .withCreatedByNominationId(nomination.getId())
        .build();

    given(nominationAccessService.getNominations(List.of(nomination.getId())))
        .willReturn(List.of(nomination));

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(nominatedAppointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", nomination.getReference());
  }

  @Test
  void getTimelineItemViews_whenAppointmentFromUnknownNomination_thenCreatedByReferenceIsUnknown() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var unknownNominationId = new NominationId(UUID.randomUUID());

    var unknownNominationAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(null)
        .withCreatedByNominationId(unknownNominationId.id())
        .build();

    given(nominationAccessService.getNominations(List.of(unknownNominationId.id())))
        .willReturn(List.of(NominationTestUtil.builder().build()));

    given(nominationAccessService.getNominations(List.of(unknownNominationId.id())))
        .willReturn(List.of(new Nomination()));

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

    var noNominationIdAppointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(null)
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

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of());

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, mode = EnumSource.Mode.INCLUDE, names = {"VIEW_ANY_NOMINATION", "NOMINATION_MANAGER"})
  void getTimelineItemViews_whenUserLoggedAndCanViewNominations_thenNominationUrlIsNotNull(
      Role rolePermission
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();
    var nominationId = new NominationId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(nominationId.id())
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var roleCanViewNomination = new TeamRole();
    roleCanViewNomination.setTeam(regulatorTeam);
    roleCanViewNomination.setRole(rolePermission);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(roleCanViewNomination));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "nominationUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(nominationId, null))
        );
  }

  @ParameterizedTest
  @EnumSource(
      value = Role.class,
      names = {"VIEW_ANY_NOMINATION", "NOMINATION_MANAGER"},
      mode = EnumSource.Mode.EXCLUDE
  )
  void getTimelineItemViews_whenUserLoggedAndCannotViewNominations_thenNominationUrlIsNull(
      Role rolePermission
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();
    var nominationId = new NominationId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(nominationId.id())
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var roleCannotViewNomination = new TeamRole();
    roleCannotViewNomination.setTeam(regulatorTeam);
    roleCannotViewNomination.setRole(rolePermission);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(roleCannotViewNomination));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainKey("nominationUrl");
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, mode = EnumSource.Mode.INCLUDE, names = {"CONSULTATION_PARTICIPANT", "CONSULTATION_MANAGER"})
  void getTimelineItemViews_whenUserLoggedAndCanConsultOnNominations_thenNominationUrlIsNotNull(Role consulteeRole) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();
    var nominationId = new NominationId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(nominationId.id())
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var consulteeTeam = new Team();
    consulteeTeam.setTeamType(TeamType.CONSULTEE);

    var consulteeTeamRole = new TeamRole();
    consulteeTeamRole.setTeam(consulteeTeam);
    consulteeTeamRole.setRole(consulteeRole);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(consulteeTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "nominationUrl",
            ReverseRouter.route(on(NominationConsulteeViewController.class)
                .renderNominationView(nominationId))
        );
  }

  @Test
  void getTimelineItemViews_whenUserLoggedAndHasPermissionToManageAppointments_thenCanManageAppointment() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withResponsibleToDate(null)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var appointmentManagerTeamRole = new TeamRole();
    appointmentManagerTeamRole.setTeam(regulatorTeam);
    appointmentManagerTeamRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(appointmentManagerTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "updateUrl",
            ReverseRouter.route(on(AppointmentCorrectionController.class)
                .renderCorrection(new AppointmentId(appointment.getId())))
        )
        .containsEntry(
            "terminateUrl",
            ReverseRouter.route(on(AppointmentTerminationController.class)
                .renderTermination(new AppointmentId(appointment.getId())))
        );
  }

  @Test
  void getTimelineItemViews_whenCanManageAppointmentsAndAppointmentIsExtant_thenVerifyRemoveUrl() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withResponsibleToDate(null)
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var appointmentManagerTeamRole = new TeamRole();
    appointmentManagerTeamRole.setTeam(regulatorTeam);
    appointmentManagerTeamRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(appointmentManagerTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry(
            "removeUrl",
            ReverseRouter.route(on(RemoveAppointmentController.class).removeAppointment(
                new AppointmentId(appointment.getId()),
                null
            )));
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentStatus.class, names = {"EXTANT"}, mode = EnumSource.Mode.EXCLUDE)
  void getTimelineItemViews_whenCanManageAppointmentsAndAppointmentIsNotExtant_thenVerifyNoRemoveUrl(
      AppointmentStatus appointmentStatus
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withResponsibleToDate(null)
        .withAppointmentStatus(appointmentStatus)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var appointmentManagerTeamRole = new TeamRole();
    appointmentManagerTeamRole.setTeam(regulatorTeam);
    appointmentManagerTeamRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(appointmentManagerTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .doesNotContainEntry(
            "removeUrl",
            ReverseRouter.route(on(RemoveAppointmentController.class).removeAppointment(
                new AppointmentId(appointment.getId()),
                null
            )));
  }

  @Test
  void getTimelineItemViews_whenUserLoggedAndDoesNotHavePermissionToManageAppointments_thenCannotManageAppointment() {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withResponsibleToDate(LocalDate.now())
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var nonRegulatorTeam = new Team();
    nonRegulatorTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    var organisationTeamRole = new TeamRole();
    organisationTeamRole.setTeam(nonRegulatorTeam);
    organisationTeamRole.setRole(Role.NOMINATION_SUBMITTER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(organisationTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withAppointmentType(appointmentType)
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withAppointmentType(AppointmentType.DEEMED)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(ServiceUserDetailTestUtil.Builder().build());

    given(userDetailService.isUserLoggedIn())
        .willReturn(true);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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
  void getAppointmentHistoryForPortalAsset_whenAuthenticated_andNotDeemed_thenNoDeemedLetter(
      AppointmentType appointmentType
  ) {

    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .withAppointmentType(appointmentType)
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(ServiceUserDetailTestUtil.Builder().build());

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .build();

    given(userDetailService.getUserDetail())
        .willThrow(InvalidAuthenticationException.class);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(UUID.randomUUID())
        .build();

    given(userDetailService.getUserDetail())
        .willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var teamManagerRole = new TeamRole();
    teamManagerRole.setTeam(regulatorTeam);
    teamManagerRole.setRole(Role.TEAM_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(teamManagerRole));

    var appointmentCorrectionHistoryView = AppointmentCorrectionHistoryViewTestUtil.builder()
        .withAppointmentId(new AppointmentId(appointment.getId()))
        .build();
    given(appointmentCorrectionService.getAppointmentCorrectionHistoryViews(
        List.of(new AppointmentId(appointment.getId()))))
        .willReturn(List.of(appointmentCorrectionHistoryView));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
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

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsNull_thenDisplayText(AppointmentType appointmentType) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(null)
        .build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", appointmentType.getScreenDisplayText());
  }

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsDeemed_thenAssetNameAndStartDate(
      AppointmentType appointmentType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.DEEMED)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);
    var startDate = DateUtil.formatLongDate(createdByAppointment.getResponsibleFromDate());

    var expectedReference = AppointmentTimelineItemService.CREATED_BY_APPOINTMENT_STRING_FORMAT
        .formatted(assetName, startDate);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference);
  }

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsOffline_andNoLegacyRef_thenAssetNameAndStartDate(
      AppointmentType appointmentType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference(null)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var manageAppointmentTeamRole = new TeamRole();
    manageAppointmentTeamRole.setTeam(regulatorTeam);
    manageAppointmentTeamRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(manageAppointmentTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);
    var startDate = DateUtil.formatLongDate(createdByAppointment.getResponsibleFromDate());

    var expectedReference = AppointmentTimelineItemService.CREATED_BY_APPOINTMENT_STRING_FORMAT
        .formatted(assetName, startDate);

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference)
        .doesNotContainKey("offlineNominationDocumentUrl");
  }

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsOffline_andLegacyRef_andIsRegulator_thenIncludeLegacyRefAndLink(
      AppointmentType appointmentType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference("OSDOP-124")
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var manageAppointmentTeamRole = new TeamRole();
    manageAppointmentTeamRole.setTeam(regulatorTeam);
    manageAppointmentTeamRole.setRole(Role.APPOINTMENT_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(manageAppointmentTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    var expectedReference = AppointmentTimelineItemService.CREATED_BY_APPOINTMENT_STRING_FORMAT
        .formatted(assetName, createdByAppointment.getCreatedByLegacyNominationReference());

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference)
        .containsEntry("offlineNominationDocumentUrl", systemOfRecordConfigurationProperties.offlineNominationDocumentUrl());
  }

  @ParameterizedTest
  @MethodSource("forwardApprovedAndParentWellboreAppointmentTypesWithNoRegulatorTeamType")
  void getTimelineItemViews_whenCreatedByAppointmentIsOffline_andLegacyRef_andIsNotRegulator_thenNoLink(
      AppointmentType appointmentType,
      TeamType teamType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.OFFLINE_NOMINATION)
        .withCreatedByLegacyNominationReference("OSDOP-124")
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var nonRegulatorTeam = new Team();
    nonRegulatorTeam.setTeamType(teamType);

    var teamManagerTeamRole = new TeamRole();
    teamManagerTeamRole.setTeam(nonRegulatorTeam);
    teamManagerTeamRole.setRole(Role.TEAM_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(teamManagerTeamRole));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    var expectedReference = AppointmentTimelineItemService.CREATED_BY_APPOINTMENT_STRING_FORMAT
        .formatted(assetName, createdByAppointment.getCreatedByLegacyNominationReference());

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference)
        .doesNotContainKey("offlineNominationDocumentUrl");
  }

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsOnline_andNominationExists_thenIncludeNominationAndLink(
      AppointmentType appointmentType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var nomination = NominationDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withCreatedByNominationId(nomination.nominationId().id())
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var manageNominationTeamRole = new TeamRole(UUID.randomUUID());
    manageNominationTeamRole.setTeam(regulatorTeam);
    manageNominationTeamRole.setRole(Role.NOMINATION_MANAGER);

    var viewAnyNominationTeamRole = new TeamRole(UUID.randomUUID());
    viewAnyNominationTeamRole.setTeam(regulatorTeam);
    viewAnyNominationTeamRole.setRole(Role.VIEW_ANY_NOMINATION);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(viewAnyNominationTeamRole, manageNominationTeamRole));

    given(nominationAccessService.getNomination(new NominationId(createdByAppointment.getCreatedByNominationId())))
        .willReturn(Optional.of(nomination));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    var expectedReference = ForwardApprovedAppointmentRestService.SEARCH_DISPLAY_STRING
        .formatted(assetName, nomination.nominationReference());

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference)
        .containsEntry("nominationUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(new NominationId(createdByAppointment.getCreatedByNominationId()), null)));
  }

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsOnline_andNominationExists_andCantViewNominations_thenNoLink(
      AppointmentType appointmentType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var nomination = NominationDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withCreatedByNominationId(nomination.nominationId().id())
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var teamManagerTeamRole = new TeamRole();
    teamManagerTeamRole.setTeam(regulatorTeam);
    teamManagerTeamRole.setRole(Role.TEAM_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(teamManagerTeamRole));

    given(nominationAccessService.getNomination(new NominationId(createdByAppointment.getCreatedByNominationId())))
        .willReturn(Optional.of(nomination));

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    var expectedReference = ForwardApprovedAppointmentRestService.SEARCH_DISPLAY_STRING
        .formatted(assetName, nomination.nominationReference());

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference)
        .doesNotContainKey("nominationUrl");
  }

  @ParameterizedTest
  @EnumSource(
      value = AppointmentType.class,
      names = {"FORWARD_APPROVED", "PARENT_WELLBORE"}
  )
  void getTimelineItemViews_whenCreatedByAppointmentIsOnline_andNoNominationExists_thenUnknownNominationReference(
      AppointmentType appointmentType
  ) {
    var assetInSystemOfRecord = AssetDtoTestUtil.builder().build();

    var createdByAppointment = AppointmentTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentType(appointmentType)
        .withCreatedByAppointmentId(createdByAppointment.getId())
        .build();

    var assetName = "asset name";
    when(portalAssetNameService.getAssetName(createdByAppointment))
        .thenReturn(Optional.of(assetName));

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getCreatedByAppointmentId())))
        .willReturn(Optional.of(createdByAppointment));

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    var regulatorTeam = new Team();
    regulatorTeam.setTeamType(TeamType.REGULATOR);

    var teamManagerTeamRole = new TeamRole();
    teamManagerTeamRole.setTeam(regulatorTeam);
    teamManagerTeamRole.setRole(Role.TEAM_MANAGER);

    given(teamQueryService.getTeamRolesForUser(loggedInUser.wuaId()))
        .willReturn(Set.of(teamManagerTeamRole));

    given(nominationAccessService.getNomination(new NominationId(createdByAppointment.getCreatedByNominationId())))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistoryItems = appointmentTimelineItemService.getTimelineItemViews(
        List.of(appointment),
        assetInSystemOfRecord
    );

    assertThat(resultingAppointmentTimelineHistoryItems).hasSize(1);

    AssetTimelineItemView timelineItemView = resultingAppointmentTimelineHistoryItems.get(0);

    var expectedReference = ForwardApprovedAppointmentRestService.SEARCH_DISPLAY_STRING
        .formatted(assetName, "Unknown");

    assertThat(timelineItemView.assetTimelineModelProperties().getModelProperties())
        .containsEntry("createdByReference", expectedReference)
        .doesNotContainKey("nominationUrl");
  }

  /**
   * Use as a method source, binds {AppointmentType, TeamType} parameters to:
   * AppointmentType: FORWARD_APPROVED, PARENT_WELLBORE
   * TeamType: All except for REGULATOR
   * @return Stream of arguments as an argument tuple of {AppointmentType, TeamType}
   */
  private static Stream<Arguments> forwardApprovedAndParentWellboreAppointmentTypesWithNoRegulatorTeamType() {
    var appointmentTypes = EnumSet.of(AppointmentType.FORWARD_APPROVED, AppointmentType.PARENT_WELLBORE);
    var teamTypes = EnumSet.complementOf(EnumSet.of(TeamType.REGULATOR));
    return appointmentTypes.stream()
        .flatMap(appointmentType -> teamTypes.stream().map(teamType -> Arguments.of(appointmentType, teamType)));
  }
}