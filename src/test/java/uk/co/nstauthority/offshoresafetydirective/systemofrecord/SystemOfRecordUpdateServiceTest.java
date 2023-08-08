package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class SystemOfRecordUpdateServiceTest {

  @Mock
  private AssetPersistenceService assetPersistenceService;

  @Mock
  private AppointmentService appointmentService;

  @Mock
  private AssetPhasePersistenceService assetPhasePersistenceService;

  @Mock
  private InstallationAssetService installationAssetService;

  @Mock
  private WellAssetService wellAssetService;

  @Mock
  private SubareaAssetService subareaAssetService;

  @InjectMocks
  private SystemOfRecordUpdateService systemOfRecordUpdateService;

  @Test
  void updateSystemOfRecordByNominationDetail_whenInstallationAssets_verifyInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var existingAsset = AssetTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var assetDto = new NominatedAssetDto(
        new PortalAssetId(existingAsset.getPortalAssetId()),
        PortalAssetType.INSTALLATION,
        new AssetName(existingAsset.getAssetName()),
        List.of("stub phase")
    );
    var appointment = AppointmentTestUtil.builder()
        .withAsset(existingAsset)
        .build();

    when(installationAssetService.getInstallationAssetDtos(nominationDetail))
        .thenReturn(List.of(assetDto));

    when(wellAssetService.getNominatedWellAssetDtos(nominationDetail))
        .thenReturn(List.of());

    when(assetPersistenceService.persistNominatedAssets(List.of(assetDto)))
        .thenReturn(List.of(existingAsset));

    when(appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(existingAsset)))
        .thenReturn(List.of(appointment));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AssetPhaseDto>> assetPhaseDtoListCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetPhasePersistenceService).createAssetPhases(assetPhaseDtoListCaptor.capture());

    assertThat(assetPhaseDtoListCaptor.getValue())
        .extracting(
            AssetPhaseDto::asset,
            AssetPhaseDto::appointment,
            AssetPhaseDto::phases
        ).containsExactly(
            tuple(existingAsset, appointment, List.of("stub phase"))
        );
  }

  @Test
  void updateSystemOfRecordByNominationDetail_whenWellAssets_verifyInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var existingAsset = AssetTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var assetDto = new NominatedAssetDto(
        new PortalAssetId(existingAsset.getPortalAssetId()),
        PortalAssetType.WELLBORE,
        new AssetName(existingAsset.getAssetName()),
        List.of("stub phase")
    );
    var appointment = AppointmentTestUtil.builder()
        .withAsset(existingAsset)
        .build();

    when(installationAssetService.getInstallationAssetDtos(nominationDetail))
        .thenReturn(List.of());

    when(wellAssetService.getNominatedWellAssetDtos(nominationDetail))
        .thenReturn(List.of(assetDto));

    when(assetPersistenceService.persistNominatedAssets(List.of(assetDto)))
        .thenReturn(List.of(existingAsset));

    when(appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(existingAsset)))
        .thenReturn(List.of(appointment));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AssetPhaseDto>> assetPhaseDtoListCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetPhasePersistenceService).createAssetPhases(assetPhaseDtoListCaptor.capture());

    assertThat(assetPhaseDtoListCaptor.getValue())
        .extracting(
            AssetPhaseDto::asset,
            AssetPhaseDto::appointment,
            AssetPhaseDto::phases
        ).containsExactly(
            tuple(existingAsset, appointment, List.of("stub phase"))
        );
  }

  @Test
  void updateSystemOfRecordByNominationDetail_whenMultipleAssets_verifyInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var installationAsset = AssetTestUtil.builder()
        .withId(UUID.randomUUID())
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var nominatedInstallationAssetDto = new NominatedAssetDto(
        new PortalAssetId(installationAsset.getPortalAssetId()),
        PortalAssetType.INSTALLATION,
        new AssetName(installationAsset.getAssetName()),
        List.of("stub installation phase")
    );
    var wellAsset = AssetTestUtil.builder()
        .withId(UUID.randomUUID())
        .withPortalAssetType(PortalAssetType.WELLBORE)
        .build();
    var nominatedWellAssetDto = new NominatedAssetDto(
        new PortalAssetId(wellAsset.getPortalAssetId()),
        PortalAssetType.WELLBORE,
        new AssetName(installationAsset.getAssetName()),
        List.of("stub well phase")
    );
    var installationAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(installationAsset)
        .build();
    var wellAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .withAsset(wellAsset)
        .build();

    when(installationAssetService.getInstallationAssetDtos(nominationDetail))
        .thenReturn(List.of(nominatedInstallationAssetDto));

    when(wellAssetService.getNominatedWellAssetDtos(nominationDetail))
        .thenReturn(List.of(nominatedWellAssetDto));

    when(assetPersistenceService.persistNominatedAssets(List.of(nominatedInstallationAssetDto, nominatedWellAssetDto)))
        .thenReturn(List.of(installationAsset, wellAsset));

    when(appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(installationAsset, wellAsset)))
        .thenReturn(List.of(installationAppointment, wellAppointment));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AssetPhaseDto>> assetPhaseDtoListCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetPhasePersistenceService).createAssetPhases(assetPhaseDtoListCaptor.capture());

    assertThat(assetPhaseDtoListCaptor.getValue())
        .extracting(
            AssetPhaseDto::asset,
            AssetPhaseDto::appointment,
            AssetPhaseDto::phases
        ).containsExactlyInAnyOrder(
            tuple(installationAsset, installationAppointment, List.of("stub installation phase")),
            tuple(wellAsset, wellAppointment, List.of("stub well phase"))
        );
  }

  @Test
  void updateSystemOfRecordByNominationDetail_whenSubareaAssets_verifyInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var existingAsset = AssetTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var assetDto = new NominatedAssetDto(
        new PortalAssetId(existingAsset.getPortalAssetId()),
        PortalAssetType.SUBAREA,
        new AssetName(existingAsset.getAssetName()),
        List.of("stub phase")
    );
    var appointment = AppointmentTestUtil.builder()
        .withAsset(existingAsset)
        .build();

    when(installationAssetService.getInstallationAssetDtos(nominationDetail))
        .thenReturn(List.of());

    when(wellAssetService.getNominatedWellAssetDtos(nominationDetail))
        .thenReturn(List.of());

    when(subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail))
        .thenReturn(List.of(assetDto));

    when(assetPersistenceService.persistNominatedAssets(List.of(assetDto)))
        .thenReturn(List.of(existingAsset));

    when(appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(existingAsset)))
        .thenReturn(List.of(appointment));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AssetPhaseDto>> assetPhaseDtoListCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetPhasePersistenceService).createAssetPhases(assetPhaseDtoListCaptor.capture());

    assertThat(assetPhaseDtoListCaptor.getValue())
        .extracting(
            AssetPhaseDto::asset,
            AssetPhaseDto::appointment,
            AssetPhaseDto::phases
        ).containsExactly(
            tuple(existingAsset, appointment, List.of("stub phase"))
        );
  }
}