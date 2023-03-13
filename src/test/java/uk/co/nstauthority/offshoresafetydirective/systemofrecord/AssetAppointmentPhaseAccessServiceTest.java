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

    var asset = AssetTestUtil.builder().build();

    var firstAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var firstAppointmentPhase = AssetPhaseTestUtil.builder()
        .withAsset(asset)
        .withAppointment(firstAppointment)
        .withPhase("FIRST_APPOINTMENT_PHASE")
        .build();

    var secondAppointment = AppointmentTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var secondAppointmentPhase = AssetPhaseTestUtil.builder()
        .withAsset(asset)
        .withAppointment(secondAppointment)
        .withPhase("SECOND_APPOINTMENT_PHASE")
        .build();

    given(assetPhaseRepository.findByAsset_Id(asset.getId()))
        .willReturn(List.of(firstAppointmentPhase, secondAppointmentPhase));

    Map<AppointmentId, List<AssetAppointmentPhase>> resultingAppointmentPhases =
        assetAppointmentPhaseAccessService.getAppointmentPhases(AssetDto.fromAsset(asset));

    assertThat(resultingAppointmentPhases)
        .containsAllEntriesOf(
            Map.of(
                new AppointmentId(firstAppointment.getId()),
                List.of(new AssetAppointmentPhase(firstAppointmentPhase.getPhase())),
                new AppointmentId(secondAppointment.getId()),
                List.of(new AssetAppointmentPhase(secondAppointmentPhase.getPhase()))
            )
        );
  }

}