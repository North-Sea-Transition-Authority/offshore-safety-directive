package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

class AppointmentDtoTest {

  @Test
  void fromAppointment_verifyMapping() {

    var appointment = AppointmentTestUtil.builder().build();
    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    PropertyObjectAssert.thenAssertThat(resultingAppointmentDto)
        .hasFieldOrPropertyWithValue("appointmentId", new AppointmentId(appointment.getId()))
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(String.valueOf(appointment.getAppointedPortalOperatorId()))
        )
        .hasFieldOrPropertyWithValue(
            "appointmentFromDate",
            new AppointmentFromDate(appointment.getResponsibleFromDate())
        )
        .hasFieldOrPropertyWithValue("appointmentToDate", new AppointmentToDate(appointment.getResponsibleToDate()))
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", appointment.getCreatedDatetime())
        .hasFieldOrPropertyWithValue("appointmentType", appointment.getAppointmentType())
        .hasFieldOrPropertyWithValue("assetDto", AssetDto.fromAsset(appointment.getAsset()))
        .hasAssertedAllPropertiesExcept("legacyNominationReference", "nominationId");
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
    var nominationId = UUID.randomUUID();

    var appointment = AppointmentTestUtil.builder()
        .withCreatedByNominationId(nominationId)
        .build();

    var resultingAppointmentDto = AppointmentDto.fromAppointment(appointment);

    assertThat(resultingAppointmentDto)
        .extracting(AppointmentDto::nominationId)
        .isEqualTo(new NominationId(nominationId));
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