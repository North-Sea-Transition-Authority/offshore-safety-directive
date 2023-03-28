package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

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
            AppointmentDto::appointmentCreatedDate,
            AppointmentDto::appointmentType
        )
        .containsExactly(
            appointment.getId(),
            appointment.getAsset().getPortalAssetId(),
            String.valueOf(appointment.getAppointedPortalOperatorId()),
            appointment.getResponsibleFromDate(),
            appointment.getResponsibleToDate(),
            appointment.getCreatedDatetime(),
            appointment.getAppointmentType()
        );
  }

  @Test
  void fromAppointment_whenLegacyNominationReference_thenVerifyMapping() {

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByLegacyNominationReference("legacy nomination reference")
        .build();

    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    assertThat(resultingAppointmentDto)
        .extracting(AppointmentDto::legacyNominationReference)
        .isEqualTo("legacy nomination reference");
  }

  @Test
  void fromAppointment_whenNoLegacyNominationReference_thenNullValueMapped() {

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByLegacyNominationReference(null)
        .build();

    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    assertThat(resultingAppointmentDto)
        .extracting(AppointmentDto::legacyNominationReference)
        .isNull();
  }

  @Test
  void fromAppointment_whenNominationId_thenVerifyMapping() {

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(100)
        .build();

    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    assertThat(resultingAppointmentDto)
        .extracting(AppointmentDto::nominationId)
        .isEqualTo(new NominationId(100));
  }

  @Test
  void fromAppointment_whenNoNominationId_thenNullValueMapped() {

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(null)
        .build();

    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    assertThat(resultingAppointmentDto)
        .extracting(AppointmentDto::nominationId)
        .isNull();
  }
}