package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsOperationType;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAddedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.message.ended.AppointmentEndedEventPublisher;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class PearsSubareaMessageHandlerServiceTest {

  @Mock
  private AssetPersistenceService assetPersistenceService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Mock
  private AppointmentRepository appointmentRepository;

  @Mock
  private AssetPhaseRepository assetPhaseRepository;

  @Mock
  private AssetAccessService assetAccessService;

  @Mock
  private AppointmentAddedEventPublisher appointmentAddedEventPublisher;

  @Mock
  private AppointmentEndedEventPublisher appointmentEndedEventPublisher;

  private Instant now;
  private PearsSubareaMessageHandlerService pearsSubareaMessageHandlerService;

  @BeforeEach
  void setUp() {
    now = Instant.now();
    var clock = Clock.fixed(now, ZoneId.systemDefault());
    var realService = new PearsSubareaMessageHandlerService(
        assetPersistenceService, appointmentAccessService, licenceBlockSubareaQueryService,
        assetAppointmentPhaseAccessService, appointmentRepository, assetPhaseRepository, clock,
        assetAccessService, appointmentAddedEventPublisher, appointmentEndedEventPublisher
    );
    pearsSubareaMessageHandlerService = spy(realService);
  }

  @Test
  void rebuildAppointmentsAndAssets() {
    var firstSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var secondSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0))
        .withSubareaChanges(Set.of(firstSubareaChange, secondSubareaChange))
        .build();

    doNothing().when(pearsSubareaMessageHandlerService).copyForwardAppointmentAndAssets(eq(operation), any());

    pearsSubareaMessageHandlerService.rebuildAppointmentsAndAssets(operation);

    verify(pearsSubareaMessageHandlerService).copyForwardAppointmentAndAssets(operation, firstSubareaChange);
    verify(pearsSubareaMessageHandlerService).copyForwardAppointmentAndAssets(operation, secondSubareaChange);
  }

  @Test
  void rebuildAppointmentsAndAssets_whenOperationTypeCannotBeMapped_thenError() {

    PearsOperationType unknownOperationType = null;

    var firstSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var secondSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(unknownOperationType)
        .withSubareaChanges(Set.of(firstSubareaChange, secondSubareaChange))
        .build();

    assertThatThrownBy(() -> pearsSubareaMessageHandlerService.rebuildAppointmentsAndAssets(operation))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unable to end appointment and assets for operation [%s] with type [%s]".formatted(
                operation.id(),
                operation.type()
            ));

    verify(pearsSubareaMessageHandlerService, never()).copyForwardAppointmentAndAssets(any(), any());
  }

  @Test
  void rebuildAppointmentsAndAssets_whenOperationTypeIsEnd_thenError() {
    var firstSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var secondSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.END.getOperations().get(0))
        .withSubareaChanges(Set.of(firstSubareaChange, secondSubareaChange))
        .build();

    assertThatThrownBy(() -> pearsSubareaMessageHandlerService.rebuildAppointmentsAndAssets(operation))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Cannot rebuild appointments for operation [%s] as [%s] operation types are not supported".formatted(
                operation.id(),
                PearsTransactionOperationType.END
            ));

    verify(pearsSubareaMessageHandlerService, never()).copyForwardAppointmentAndAssets(any(), any());
  }

  @Test
  void copyForwardAppointmentAndAssets_whenResultingSubareasDoesNotContainOriginalSubarea_thenVerifyCalls() {
    var portalAssetId = new PortalAssetId(UUID.randomUUID().toString());
    var originalSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(portalAssetId.id());
    var firstResultingSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var secondResultingSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var subareaChange = PearsTransactionSubareaChangeTestUtil.builder()
        .withOriginalSubarea(originalSubarea)
        .withResultingSubareas(Set.of(firstResultingSubarea, secondResultingSubarea))
        .build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0))
        .withSubareaChanges(Set.of(subareaChange))
        .build();

    var persistedAssetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getExtantAsset(portalAssetId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.of(persistedAssetDto));

    var firstLicenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();
    var secondLicenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(
            new LicenceBlockSubareaId(firstResultingSubarea.id()),
            new LicenceBlockSubareaId(secondResultingSubarea.id())
        ),
        PearsSubareaMessageHandlerService.LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(firstLicenceBlockSubareaDto, secondLicenceBlockSubareaDto));

    var firstAsset = AssetTestUtil.builder().build();
    var secondAsset = AssetTestUtil.builder().build();
    when(assetPersistenceService.createAssetsForSubareas(
        List.of(firstLicenceBlockSubareaDto, secondLicenceBlockSubareaDto))
    )
        .thenReturn(List.of(firstAsset, secondAsset));

    var firstAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var secondAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(appointmentAccessService.getAppointmentsForAsset(persistedAssetDto.assetId()))
        .thenReturn(List.of(firstAppointment, secondAppointment));

    var assetPhase = AssetPhaseTestUtil.builder().build();
    var appointmentIdAndPhaseListMap = Map.of(
        new AppointmentId(firstAppointment.getId()),
        List.of(assetPhase)
    );
    when(assetAppointmentPhaseAccessService.getPhasesByAppointments(List.of(firstAppointment, secondAppointment)))
        .thenReturn(appointmentIdAndPhaseListMap);

    doNothing().when(pearsSubareaMessageHandlerService).rebuildAppointments(
        List.of(firstAppointment, secondAppointment),
        appointmentIdAndPhaseListMap,
        List.of(firstAsset, secondAsset)
    );

    pearsSubareaMessageHandlerService.copyForwardAppointmentAndAssets(operation, subareaChange);

    verify(assetPersistenceService).endAssetsWithAssetType(
        List.of(portalAssetId),
        PortalAssetType.SUBAREA,
        operation.id()
    );

    verify(appointmentEndedEventPublisher).publish(firstAppointment.getId());
    verify(appointmentEndedEventPublisher).publish(secondAppointment.getId());

    verify(appointmentRepository).saveAll(List.of(firstAppointment, secondAppointment));
  }

  @Test
  void copyForwardAppointmentAndAssets_whenResultingSubareasContainsOriginalSubarea_thenVerifyCalls() {
    var portalAssetId = new PortalAssetId(UUID.randomUUID().toString());
    var originalSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(portalAssetId.id());
    var firstResultingSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(portalAssetId.id());
    var secondResultingSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var subareaChange = PearsTransactionSubareaChangeTestUtil.builder()
        .withOriginalSubarea(originalSubarea)
        .withResultingSubareas(Set.of(firstResultingSubarea, secondResultingSubarea))
        .build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0))
        .withSubareaChanges(Set.of(subareaChange))
        .build();

    var persistedAssetDto = AssetDtoTestUtil.builder().build();
    when(assetAccessService.getExtantAsset(portalAssetId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.of(persistedAssetDto));

    var licenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        Set.of(
            new LicenceBlockSubareaId(secondResultingSubarea.id())
        ),
        PearsSubareaMessageHandlerService.LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    ))
        .thenReturn(List.of(licenceBlockSubareaDto));

    var asset = AssetTestUtil.builder().build();
    when(assetPersistenceService.createAssetsForSubareas(
        List.of(licenceBlockSubareaDto))
    )
        .thenReturn(List.of(asset));

    var appointment = AppointmentTestUtil.builder().build();
    when(appointmentAccessService.getAppointmentsForAsset(persistedAssetDto.assetId()))
        .thenReturn(List.of(appointment));

    var assetPhase = AssetPhaseTestUtil.builder().build();
    var appointmentIdAndPhaseListMap = Map.of(
        new AppointmentId(appointment.getId()),
        List.of(assetPhase)
    );
    when(assetAppointmentPhaseAccessService.getPhasesByAppointments(List.of(appointment)))
        .thenReturn(appointmentIdAndPhaseListMap);

    doNothing().when(pearsSubareaMessageHandlerService).rebuildAppointments(
        List.of(appointment),
        appointmentIdAndPhaseListMap,
        List.of(asset)
    );

    pearsSubareaMessageHandlerService.copyForwardAppointmentAndAssets(operation, subareaChange);

    verify(appointmentRepository).saveAll(List.of(appointment));
  }

  @Test
  void copyForwardAppointmentAndAssets_whenResultingSubareaContainsOnlyOriginalSubarea_thenVerifyNoInteractions() {
    var portalAssetId = new PortalAssetId(UUID.randomUUID().toString());
    var originalSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(portalAssetId.id());
    var firstResultingSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(portalAssetId.id());
    var subareaChange = PearsTransactionSubareaChangeTestUtil.builder()
        .withOriginalSubarea(originalSubarea)
        .withResultingSubareas(Set.of(firstResultingSubarea))
        .build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0))
        .withSubareaChanges(Set.of(subareaChange))
        .build();

    verifyNoInteractions(
        assetPersistenceService,
        licenceBlockSubareaQueryService,
        appointmentAccessService,
        assetAppointmentPhaseAccessService,
        appointmentRepository
    );

    pearsSubareaMessageHandlerService.copyForwardAppointmentAndAssets(operation, subareaChange);
  }

  @Test
  void rebuildAppointments() {
    var firstAppointment = AppointmentTestUtil.builder().build();
    var secondAppointment = AppointmentTestUtil.builder().build();

    var firstAddedAppointment = AppointmentTestUtil.builder().build();
    var secondAddedAppointment = AppointmentTestUtil.builder().build();
    var thirdAddedAppointment = AppointmentTestUtil.builder().build();
    var fourthAddedAppointment = AppointmentTestUtil.builder().build();

    var firstAssetPhase = AssetPhaseTestUtil.builder().build();
    var secondAssetPhase = AssetPhaseTestUtil.builder().build();

    var firstAddedAssetPhase = AssetPhaseTestUtil.builder().build();
    var secondAddedAssetPhase = AssetPhaseTestUtil.builder().build();
    var thirdAddedAssetPhase = AssetPhaseTestUtil.builder().build();
    var fourthAddedAssetPhase = AssetPhaseTestUtil.builder().build();

    var appointmentIdAndPhaseListMap = Map.of(
        new AppointmentId(firstAppointment.getId()),
        List.of(firstAssetPhase),
        new AppointmentId(secondAppointment.getId()),
        List.of(secondAssetPhase)
    );

    var firstAsset = AssetTestUtil.builder().build();
    var secondAsset = AssetTestUtil.builder().build();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<Appointment>> persistAppointmentListCaptor = ArgumentCaptor.forClass(Collection.class);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<AssetPhase>> persistAssetPhaseListCaptor = ArgumentCaptor.forClass(Collection.class);

    doAnswer(invocation -> {
      persistAppointmentListCaptor.getValue().add(firstAddedAppointment);
      persistAssetPhaseListCaptor.getValue().add(firstAddedAssetPhase);
      return invocation;
    })
        .when(pearsSubareaMessageHandlerService)
        .duplicateAppointment(
            eq(firstAppointment),
            any(),
            persistAppointmentListCaptor.capture(),
            persistAssetPhaseListCaptor.capture(),
            eq(firstAsset)
        );

    doAnswer(invocation -> {
      persistAppointmentListCaptor.getValue().add(secondAddedAppointment);
      persistAssetPhaseListCaptor.getValue().add(secondAddedAssetPhase);
      return invocation;
    })
        .when(pearsSubareaMessageHandlerService)
        .duplicateAppointment(
            eq(secondAppointment),
            any(),
            persistAppointmentListCaptor.capture(),
            persistAssetPhaseListCaptor.capture(),
            eq(firstAsset)
        );

    doAnswer(invocation -> {
      persistAppointmentListCaptor.getValue().add(thirdAddedAppointment);
      persistAssetPhaseListCaptor.getValue().add(thirdAddedAssetPhase);
      return invocation;
    })
        .when(pearsSubareaMessageHandlerService)
        .duplicateAppointment(
            eq(firstAppointment),
            any(),
            persistAppointmentListCaptor.capture(),
            persistAssetPhaseListCaptor.capture(),
            eq(secondAsset)
        );

    doAnswer(invocation -> {
      persistAppointmentListCaptor.getValue().add(fourthAddedAppointment);
      persistAssetPhaseListCaptor.getValue().add(fourthAddedAssetPhase);
      return invocation;
    })
        .when(pearsSubareaMessageHandlerService)
        .duplicateAppointment(
            eq(secondAppointment),
            any(),
            persistAppointmentListCaptor.capture(),
            persistAssetPhaseListCaptor.capture(),
            eq(secondAsset)
        );

    pearsSubareaMessageHandlerService.rebuildAppointments(
        List.of(firstAppointment, secondAppointment),
        appointmentIdAndPhaseListMap,
        List.of(firstAsset, secondAsset)
    );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<Appointment>> savedAppointmentListCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(appointmentRepository).saveAll(savedAppointmentListCaptor.capture());

    assertThat(savedAppointmentListCaptor.getValue())
        .containsExactly(
            firstAddedAppointment,
            secondAddedAppointment,
            thirdAddedAppointment,
            fourthAddedAppointment
        );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<AssetPhase>> savedAssetPhaseListCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(assetPhaseRepository).saveAll(savedAssetPhaseListCaptor.capture());

    assertThat(savedAssetPhaseListCaptor.getValue())
        .containsExactlyInAnyOrder(
            firstAddedAssetPhase,
            secondAddedAssetPhase,
            thirdAddedAssetPhase,
            fourthAddedAssetPhase
        );

    verify(appointmentAddedEventPublisher).publish(new AppointmentId(firstAddedAppointment.getId()));
    verify(appointmentAddedEventPublisher).publish(new AppointmentId(secondAddedAppointment.getId()));
    verify(appointmentAddedEventPublisher).publish(new AppointmentId(thirdAddedAppointment.getId()));
    verify(appointmentAddedEventPublisher).publish(new AppointmentId(fourthAddedAppointment.getId()));

  }

  @Test
  void duplicateAppointments() {
    var appointment = AppointmentTestUtil.builder().build();
    var asset = AssetTestUtil.builder().build();

    var firstPhase = AssetPhaseTestUtil.builder().build();
    var secondPhase = AssetPhaseTestUtil.builder().build();

    var appointmentList = new ArrayList<Appointment>();
    var assetPhaseList = new ArrayList<AssetPhase>();

    var appointmentIdAndPhaseListMap = Map.of(
        new AppointmentId(appointment.getId()),
        List.of(firstPhase, secondPhase)
    );

    doNothing()
        .when(pearsSubareaMessageHandlerService)
        .duplicateAssetPhases(any(), any(), any());

    pearsSubareaMessageHandlerService.duplicateAppointment(
        appointment,
        appointmentIdAndPhaseListMap,
        appointmentList,
        assetPhaseList,
        asset
    );

    ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

    verify(pearsSubareaMessageHandlerService).duplicateAssetPhases(
        eq(firstPhase),
        eq(assetPhaseList),
        appointmentCaptor.capture()
    );

    verify(pearsSubareaMessageHandlerService).duplicateAssetPhases(
        eq(secondPhase),
        eq(assetPhaseList),
        eq(appointmentCaptor.getValue())
    );

    // Verify these were the only two calls
    verify(pearsSubareaMessageHandlerService, times(2)).duplicateAssetPhases(any(), any(), any());

    assertThat(appointmentCaptor.getValue())
        .usingRecursiveComparison()
        .ignoringFields("id", "createdByAppointmentId", "asset")
        .isEqualTo(appointment);

    assertThat(appointmentCaptor.getValue())
        .extracting(
            Appointment::getAsset,
            Appointment::getCreatedByAppointmentId
        )
        .containsExactly(
            asset,
            appointment.getId()
        );

    assertThat(appointmentList).containsExactly(appointmentCaptor.getValue());
  }

  @Test
  void duplicateAssetPhases() {
    var assetPhase = AssetPhaseTestUtil.builder().build();
    var assetPhaseList = new ArrayList<AssetPhase>();
    var newAppointment = AppointmentTestUtil.builder().build();

    pearsSubareaMessageHandlerService.duplicateAssetPhases(
        assetPhase,
        assetPhaseList,
        newAppointment
    );

    assertThat(assetPhaseList)
        .hasSize(1)
        .first()
        .extracting(
            AssetPhase::getAppointment,
            AssetPhase::getAsset,
            AssetPhase::getPhase
        )
        .containsExactly(
            newAppointment,
            newAppointment.getAsset(),
            assetPhase.getPhase()
        );
  }

  @Test
  void endAppointmentsAndAssets_whenOperationTypeCannotBeMapped_thenError() {
    var firstSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var secondSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();

    PearsOperationType unknownOperationType = null;

    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(unknownOperationType)
        .withSubareaChanges(Set.of(firstSubareaChange, secondSubareaChange))
        .build();

    assertThatThrownBy(() -> pearsSubareaMessageHandlerService.endAppointmentsAndAssets(operation))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unable to end appointment and assets for operation [%s] with type [%s]".formatted(
                operation.id(),
                operation.type()
            ));

    verify(pearsSubareaMessageHandlerService, never()).copyForwardAppointmentAndAssets(any(), any());
  }

  @Test
  void endAppointmentsAndAssets_whenOperationTypeIsCopyForward_thenError() {
    var firstSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var secondSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0))
        .withSubareaChanges(Set.of(firstSubareaChange, secondSubareaChange))
        .build();
    
    assertThatThrownBy(() -> pearsSubareaMessageHandlerService.endAppointmentsAndAssets(operation))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Cannot rebuild appointments for operation [%s] as [%s] operation types are not supported".formatted(
                operation.id(),
                PearsTransactionOperationType.COPY_FORWARD
            ));

    verify(pearsSubareaMessageHandlerService, never()).copyForwardAppointmentAndAssets(any(), any());
  }

  @Test
  void endAppointmentsAndAssets() {
    var firstSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var secondSubareaChange = PearsTransactionSubareaChangeTestUtil.builder().build();
    var operation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.END.getOperations().get(0))
        .withSubareaChanges(Set.of(firstSubareaChange, secondSubareaChange))
        .build();

    var firstSubareaPortalId = new PortalAssetId(firstSubareaChange.originalSubarea().id());
    var secondSubareaPortalId = new PortalAssetId(secondSubareaChange.originalSubarea().id());

    var firstAssetDto = AssetDtoTestUtil.builder().build();
    var secondAssetDto = AssetDtoTestUtil.builder().build();

    when(assetAccessService.getExtantAsset(firstSubareaPortalId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.of(firstAssetDto));

    when(assetAccessService.getExtantAsset(secondSubareaPortalId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.of(secondAssetDto));

    var firstActiveAppointment = spy(AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .withId(UUID.randomUUID())
        .build());

    var secondActiveAppointment = spy(AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build());

    when(appointmentAccessService.getCurrentAppointmentForAsset(firstAssetDto.assetId()))
        .thenReturn(Optional.of(firstActiveAppointment));

    when(appointmentAccessService.getCurrentAppointmentForAsset(secondAssetDto.assetId()))
        .thenReturn(Optional.of(secondActiveAppointment));

    pearsSubareaMessageHandlerService.endAppointmentsAndAssets(operation);

    verify(assetPersistenceService)
        .endAssetsWithAssetType(List.of(firstSubareaPortalId),
            PortalAssetType.SUBAREA,
            operation.id()
        );

    verify(assetPersistenceService)
        .endAssetsWithAssetType(List.of(secondSubareaPortalId),
            PortalAssetType.SUBAREA,
            operation.id()
        );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentListCaptor = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository).saveAll(appointmentListCaptor.capture());

    assertThat(appointmentListCaptor.getValue())
        .containsExactlyInAnyOrder(firstActiveAppointment, secondActiveAppointment);

    verify(firstActiveAppointment).setResponsibleToDate(LocalDate.ofInstant(now, ZoneId.systemDefault()));
    verify(secondActiveAppointment).setResponsibleToDate(LocalDate.ofInstant(now, ZoneId.systemDefault()));

    verify(appointmentEndedEventPublisher).publish(firstActiveAppointment.getId());
    verify(appointmentEndedEventPublisher).publish(secondActiveAppointment.getId());
  }

  @Test
  void endAppointmentsAndAssets_whenOneExtantAssetAndOneNonExtant_thenOnlyExtantAssetEnded() {

    var knownSubareaEndOperation = PearsTransactionSubareaChangeTestUtil.builder()
        .withResultingSubareas(Set.of())
        .build();

    var unknownSubareaEndOperation = PearsTransactionSubareaChangeTestUtil.builder()
        .withResultingSubareas(Set.of())
        .build();

    // use a linked hash set to preserve order of operations, so we can be sure tha if
    // the first change is for an unknown asset we still process the second change.
    Set<PearsTransaction.Operation.SubareaChange> subareaChanges = new LinkedHashSet<>();
    subareaChanges.add(unknownSubareaEndOperation);
    subareaChanges.add(knownSubareaEndOperation);

    var endOperation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.END.getOperations().get(0))
        .withSubareaChanges(subareaChanges)
        .build();

    var knownSubareaPortalId = new PortalAssetId(knownSubareaEndOperation.originalSubarea().id());
    var unknownSubareaPortalId = new PortalAssetId(unknownSubareaEndOperation.originalSubarea().id());

    when(assetAccessService.getExtantAsset(unknownSubareaPortalId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.empty());

    var knownSubareaAsset = AssetDtoTestUtil.builder().build();

    when(assetAccessService.getExtantAsset(knownSubareaPortalId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.of(knownSubareaAsset));

    pearsSubareaMessageHandlerService.endAppointmentsAndAssets(endOperation);

    verify(assetPersistenceService, never()).endAssetsWithAssetType(
        List.of(unknownSubareaPortalId),
        PortalAssetType.SUBAREA,
        endOperation.id()
    );

    verify(appointmentAccessService).getCurrentAppointmentForAsset(knownSubareaAsset.assetId());
    verifyNoMoreInteractions(appointmentAccessService);
  }

  @Test
  void copyForwardAppointmentAndAssets_whenNonExtantAsset_thenNoCopyForward() {

    var unknownSubareaChangeOperation = PearsTransactionSubareaChangeTestUtil.builder()
        .withResultingSubareas(Set.of())
        .build();

    var copyForwardOperation = PearsTransactionOperationTestUtil.builder()
        .withType(PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0))
        .withSubareaChanges(Set.of(unknownSubareaChangeOperation))
        .build();

    var unknownSubareaPortalId = new PortalAssetId(unknownSubareaChangeOperation.originalSubarea().id());

    when(assetAccessService.getExtantAsset(unknownSubareaPortalId, PortalAssetType.SUBAREA))
        .thenReturn(Optional.empty());

    pearsSubareaMessageHandlerService.copyForwardAppointmentAndAssets(copyForwardOperation, unknownSubareaChangeOperation);

    verifyNoInteractions(appointmentRepository);
    verifyNoInteractions(assetPersistenceService);
    verifyNoInteractions(assetAppointmentPhaseAccessService);
    verifyNoInteractions(appointmentAccessService);
    verifyNoInteractions(assetPersistenceService);
    verifyNoInteractions(licenceBlockSubareaQueryService);
  }
}