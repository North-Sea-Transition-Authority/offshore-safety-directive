package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetAppointmentPhaseAccessServiceTest {

  @Mock
  private AssetPhaseRepository assetPhaseRepository;

  @InjectMocks
  private AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;

  @Test
  void getAppointmentPhases_whenAssetPhasesNotFound_thenEmptyMap() {

    var asset = AssetDtoTestUtil.builder().build();

    given(assetPhaseRepository.findByAsset_Id(asset.assetId().id()))
        .willReturn(Collections.emptyList());

    Map<AppointmentId, List<AssetAppointmentPhase>> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getAppointmentPhases(asset);

    assertThat(resultingAppointmentPhases).isEmpty();
  }

  @Test
  void getAppointmentPhases_whenAssetPhasesFound_thenGroupedByAppointment() {

    var asset = AssetDtoTestUtil.builder().build();

    var firstAppointmentId = new AppointmentId(UUID.randomUUID());

    var firstAppointmentPhase = new AssetPhaseTestProjection(
        asset.assetId().id(),
        firstAppointmentId.id(),
        "FIRST_APPOINTMENT_PHASE"
    );

    var secondAppointmentId = new AppointmentId(UUID.randomUUID());

    var secondAppointmentPhase = new AssetPhaseTestProjection(
        asset.assetId().id(),
        secondAppointmentId.id(),
        "SECOND_APPOINTMENT_PHASE"
    );

    given(assetPhaseRepository.findByAsset_Id(asset.assetId().id()))
        .willReturn(List.of(firstAppointmentPhase, secondAppointmentPhase));

    Map<AppointmentId, List<AssetAppointmentPhase>> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getAppointmentPhases(asset);

    assertThat(resultingAppointmentPhases)
        .containsAllEntriesOf(
            Map.of(
                firstAppointmentId, List.of(new AssetAppointmentPhase(firstAppointmentPhase.getPhase())),
                secondAppointmentId, List.of(new AssetAppointmentPhase(secondAppointmentPhase.getPhase()))
            )
        );
  }

  static class AssetPhaseTestProjection implements AssetPhaseProjection {

    private final UUID assetId;

    private final UUID appointmentId;

    private final String phase;

    AssetPhaseTestProjection(UUID assetId, UUID appointmentId, String phase) {
      this.assetId = assetId;
      this.appointmentId = appointmentId;
      this.phase = phase;
    }

    @Override
    public UUID getAssetId() {
      return assetId;
    }

    @Override
    public UUID getAppointmentId() {
      return appointmentId;
    }

    @Override
    public String getPhase() {
      return phase;
    }
  }

}