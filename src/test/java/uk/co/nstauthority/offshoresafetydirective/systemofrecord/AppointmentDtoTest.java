package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AppointmentDtoTest {

  @Test
  void fromAppointment_verifyMapping() {

    var appointment = AppointmentTestUtil.builder().build();

    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    assertThat(resultingAppointmentDto)
        .extracting(
            appointmentDto -> appointmentDto.appointmentId().id(),
            appointmentDto -> appointmentDto.portalAssetId().id(),
            appointmentDto -> appointmentDto.appointedOperatorId().id(),
            appointmentDto -> appointmentDto.appointmentFromDate().value(),
            appointmentDto -> appointmentDto.appointmentToDate().value(),
            AppointmentDto::appointmentCreatedDate
        )
        .containsExactly(
            appointment.getId(),
            appointment.getAsset().getPortalAssetId(),
            String.valueOf(appointment.getAppointedPortalOperatorId()),
            appointment.getResponsibleFromDate(),
            appointment.getResponsibleToDate(),
            appointment.getCreatedDatetime()
        );
  }
}