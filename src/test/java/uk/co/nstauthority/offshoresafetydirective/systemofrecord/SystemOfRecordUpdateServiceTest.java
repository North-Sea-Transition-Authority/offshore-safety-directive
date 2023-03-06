package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

  @InjectMocks
  private SystemOfRecordUpdateService systemOfRecordUpdateService;

  @Test
  void updateSystemOfRecordByNominationDetail_whenNoAssets_verifyNoInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);

    when(installationAssetService.getInstallationAssetDtos(nominationDetail))
        .thenReturn(List.of());

    systemOfRecordUpdateService.updateSystemOfRecordByNominationDetail(nominationDetail, confirmationDate);

    verifyNoInteractions(assetPersistenceService, appointmentService, assetPhasePersistenceService);
  }

  @Test
  void updateSystemOfRecordByNominationDetail_whenAssets_verifyInteractions() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var existingAsset = AssetTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var assetDto = new NominatedAssetDto(
        new PortalAssetId(existingAsset.getPortalAssetId()),
        PortalAssetType.INSTALLATION,
        List.of("stub phase")
    );
    var appointment = AppointmentTestUtil.builder()
        .withAsset(existingAsset)
        .build();

    when(installationAssetService.getInstallationAssetDtos(nominationDetail))
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