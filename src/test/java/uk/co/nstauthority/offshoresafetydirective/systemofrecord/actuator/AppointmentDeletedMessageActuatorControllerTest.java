package uk.co.nstauthority.offshoresafetydirective.systemofrecord.actuator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentSnsService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;

class AppointmentDeletedMessageActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT =
      "/actuator/appointment-deleted-message/%s";

  @MockitoBean
  private AppointmentAccessService appointmentAccessService;

  @MockitoBean
  private AppointmentSnsService appointmentSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @SecurityTest
  void whenNotUnauthorized() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var actuatorUrl = PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(post(actuatorUrl).with(httpBasic("admin", "invalid-password")))
        .andExpect(status().isUnauthorized());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }

  @Test
  void whenAppointmentNotFound() throws Exception {

    var appointmentId = UUID.randomUUID();

    var actuatorUrl = PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(
        post(actuatorUrl).with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isBadRequest());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }

  @Test
  void whenAppointmentActive() throws Exception {

    var appointmentId = UUID.randomUUID();

    var activeAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .withResponsibleToDate(null)
        .build();

    given(appointmentAccessService.getAppointment(new AppointmentId(appointmentId)))
        .willReturn(Optional.of(activeAppointment));

    var actuatorUrl = PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(
        post(actuatorUrl).with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isBadRequest());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }

  @ParameterizedTest
  @MethodSource("getDeletedAppointments")
  void whenAppointmentDeleted(Appointment appointment) throws Exception {

    given(appointmentAccessService.getAppointment(new AppointmentId(appointment.getId())))
        .willReturn(Optional.of(appointment));

    var actuatorUrl = PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointment.getId());

    mockMvc.perform(
        post(actuatorUrl)
            .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, "correlation-id")
            .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isOk());

    then(appointmentSnsService)
        .should()
        .publishAppointmentDeletedSnsMessage(appointment.getId(), "correlation-id");
  }

  private static Stream<Arguments> getDeletedAppointments() {

    var endedAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .withResponsibleToDate(LocalDate.now())
        .build();

    var terminatedAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.TERMINATED)
        .withResponsibleToDate(LocalDate.now())
        .build();

    var removedAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.REMOVED)
        .withResponsibleToDate(null)
        .build();

    return Stream.of(
        Arguments.of(Named.named("Extant appointment with end date", endedAppointment)),
        Arguments.of(Named.named("Terminated appointment with end date", terminatedAppointment)),
        Arguments.of(Named.named("Removed appointment without end date", removedAppointment))
    );
  }

}