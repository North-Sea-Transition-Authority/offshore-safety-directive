package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentAccessServiceTest {

  @Mock
  private AppointmentRepository appointmentRepository;

  @InjectMocks
  private AppointmentAccessService appointmentAccessService;

  @Test
  void getAppointmentsForAsset_whenNoAppointments_themEmptyListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    given(appointmentRepository.findAllByAsset_id(assetId.id()))
        .willReturn(Collections.emptyList());

    var resultingAppointments = appointmentAccessService.getAppointmentsForAsset(assetId);

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void getAppointmentsForAsset_whenAppointments_thenPopulatedListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    var expectedAppointment = AppointmentTestUtil.builder().build();

    given(appointmentRepository.findAllByAsset_id(assetId.id()))
        .willReturn(List.of(expectedAppointment));

    var resultingAppointments = appointmentAccessService.getAppointmentsForAsset(assetId);

    assertThat(resultingAppointments)
        .extracting(
            appointmentDto -> appointmentDto.appointmentId().id(),
            appointmentDto -> appointmentDto.portalAssetId().id(),
            appointmentDto -> appointmentDto.appointedOperatorId().id(),
            appointmentDto -> appointmentDto.appointmentFromDate().value(),
            appointmentDto -> appointmentDto.appointmentToDate().value(),
            AppointmentDto::appointmentCreatedDate
        )
        .containsExactly(
            tuple(
                expectedAppointment.getId(),
                expectedAppointment.getAsset().getPortalAssetId(),
                String.valueOf(expectedAppointment.getAppointedPortalOperatorId()),
                expectedAppointment.getResponsibleFromDate(),
                expectedAppointment.getResponsibleToDate(),
                expectedAppointment.getCreatedDatetime()
            )
        );
  }

  @Test
  void getAppointment_appointmentFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder().withId(appointmentId.id()).build();

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.of(appointment));

    assertThat(appointmentAccessService.getAppointment(appointmentId)).contains(appointment);
  }

  @Test
  void getAppointment_appointmentNotFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.empty());

    assertThat(appointmentAccessService.getAppointment(appointmentId)).isEmpty();
  }
}
