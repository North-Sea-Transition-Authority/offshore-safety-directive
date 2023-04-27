package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;

class AppointmentActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT =
      "/actuator/appointments/publish-epmq-message/appointment/%s";

  @MockBean
  private AppointmentAccessService appointmentAccessService;

  @MockBean
  private AppointmentSnsService appointmentSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @Test
  void publishAppointmentConfirmedMessage() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder().withId(appointmentId.id()).build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
        )
        .andExpect(status().isOk());

    verify(appointmentSnsService).publishAppointmentConfirmedSnsMessage(appointment);
  }

  @Test
  void publishAppointmentConfirmedMessage_appointmentNotFound() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
        )
        .andExpect(status().isNotFound());

    verify(appointmentSnsService, never()).publishAppointmentConfirmedSnsMessage(any());
  }

  @SecurityTest
  void publishAppointmentConfirmedMessage_notAuthorised() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", "invalidpassword"))
        )
        .andExpect(status().isUnauthorized());
  }
}
