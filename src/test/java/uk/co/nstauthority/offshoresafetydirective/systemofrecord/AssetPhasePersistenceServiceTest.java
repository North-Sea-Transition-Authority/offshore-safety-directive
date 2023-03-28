package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;

@ExtendWith(MockitoExtension.class)
class AssetPhasePersistenceServiceTest {

  @Mock
  private AssetPhaseRepository assetPhaseRepository;

  @InjectMocks
  private AssetPhasePersistenceService assetPhasePersistenceService;

  @Test
  void createAssetPhases() {
    var asset = AssetTestUtil.builder().build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();
    var developmentPhase = InstallationPhase.DEVELOPMENT_INSTALLATION.name();
    var designPhase = InstallationPhase.DEVELOPMENT_DESIGN.name();
    var dto = new AssetPhaseDto(asset, appointment, List.of(developmentPhase, designPhase));
    assetPhasePersistenceService.createAssetPhases(List.of(dto));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AssetPhase>> assetPhaseCaptor = ArgumentCaptor.forClass(List.class);
    verify(assetPhaseRepository).saveAll(assetPhaseCaptor.capture());

    assertThat(assetPhaseCaptor.getValue())
        .extracting(
            AssetPhase::getAsset,
            AssetPhase::getAppointment,
            AssetPhase::getPhase
        ).containsExactlyInAnyOrder(
            tuple(asset, appointment, developmentPhase),
            tuple(asset, appointment, designPhase)
        );
  }
}