package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
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

  @Mock
  private AppointmentRepository appointmentRepository;

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

  @Test
  void updateAssetPhases() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .build();

    var assetPhaseToRemove = AssetPhaseTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var appointment = AppointmentTestUtil.builder().build();
    when(appointmentRepository.findById(appointmentId.id()))
        .thenReturn(Optional.of(appointment));

    when(assetPhaseRepository.findAllByAppointment(appointment))
        .thenReturn(List.of(assetPhaseToRemove));

    var phasesToSave = List.of(
        new AssetAppointmentPhase("phase 1"),
        new AssetAppointmentPhase("phase 2")
    );

    assetPhasePersistenceService.updateAssetPhases(appointmentDto, phasesToSave);

    verify(assetPhaseRepository).deleteAll(List.of(assetPhaseToRemove));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AssetPhase>> captor = ArgumentCaptor.forClass(List.class);

    verify(assetPhaseRepository).saveAll(captor.capture());

    assertThat(captor.getValue())
        .extracting(AssetPhase::getAppointment, AssetPhase::getAsset, AssetPhase::getPhase)
        .containsExactly(
            Tuple.tuple(appointment, appointment.getAsset(), phasesToSave.get(0).value()),
            Tuple.tuple(appointment, appointment.getAsset(), phasesToSave.get(1).value())
        );
  }

  @Test
  void updateAssetPhases_whenNoAppointmentFound_thenError() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId.id())
        .build();

    when(appointmentRepository.findById(appointmentId.id()))
        .thenReturn(Optional.empty());

    var phasesToSave = List.of(
        new AssetAppointmentPhase("phase 1"),
        new AssetAppointmentPhase("phase 2")
    );

    assertThatThrownBy(() -> assetPhasePersistenceService.updateAssetPhases(appointmentDto, phasesToSave))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No appointment found with id [%s]".formatted(
            appointmentDto.appointmentId().id()
        ));
  }
}