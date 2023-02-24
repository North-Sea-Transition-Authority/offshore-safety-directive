package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewTestUtil;

@ExtendWith(MockitoExtension.class)
class SystemOfRecordUpdateServiceTest {

  @Mock
  private AssetPersistenceService assetPersistenceService;

  @Mock
  private NominatedInstallationDetailViewService nominatedInstallationDetailViewService;

  @Mock
  private AppointmentService appointmentService;

  @Mock
  private AssetPhasePersistenceService assetPhasePersistenceService;

  @InjectMocks
  private SystemOfRecordUpdateService systemOfRecordUpdateService;

  @Test
  void updateSystemOfRecordByNominationDetail_whenForAllInstallationPhases_verifyCalledForAllPhases() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);

    var installationDetailView = NominatedInstallationDetailViewTestUtil.builder()
        .withForAllInstallationPhases(true)
        .build();
    var asset = AssetTestUtil.builder().build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    var expectedPhaseNames = Arrays.stream(InstallationPhase.values())
        .map(Enum::name)
        .toList();
    var expectedAssetPhaseCreationDto = new AssetPhaseDto(asset, appointment, expectedPhaseNames);

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.of(installationDetailView));

    when(assetPersistenceService.getExistingOrCreateAssets(installationDetailView))
        .thenReturn(List.of(asset));

    when(appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(asset)))
        .thenReturn(List.of(appointment));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    verify(assetPhasePersistenceService).createAssetPhases(List.of(expectedAssetPhaseCreationDto));

  }

  @Test
  void updateSystemOfRecordByNominationDetail_whenSpecificInstallationPhases_verifyCalledForPhases() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);

    var installationDetailView = NominatedInstallationDetailViewTestUtil.builder()
        .withForAllInstallationPhases(false)
        .addInstallationPhase(InstallationPhase.DEVELOPMENT_INSTALLATION)
        .addInstallationPhase(InstallationPhase.DECOMMISSIONING)
        .build();
    var asset = AssetTestUtil.builder().build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    var expectedPhaseNames = Stream.of(InstallationPhase.DEVELOPMENT_INSTALLATION, InstallationPhase.DECOMMISSIONING)
        .map(Enum::name)
        .toList();
    var expectedAssetPhaseCreationDto = new AssetPhaseDto(asset, appointment, expectedPhaseNames);

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.of(installationDetailView));

    when(assetPersistenceService.getExistingOrCreateAssets(installationDetailView))
        .thenReturn(List.of(asset));

    when(appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(asset)))
        .thenReturn(List.of(appointment));

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    verify(assetPhasePersistenceService).createAssetPhases(List.of(expectedAssetPhaseCreationDto));

  }

  @Test
  void updateSystemOfRecordByNominationDetail_whenNoInstallationDetailView_verifyNoInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.empty());

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    verifyNoInteractions(assetPersistenceService, appointmentService, assetPhasePersistenceService);
  }
}