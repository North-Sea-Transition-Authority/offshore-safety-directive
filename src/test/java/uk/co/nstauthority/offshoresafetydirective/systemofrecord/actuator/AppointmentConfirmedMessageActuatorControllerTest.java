package uk.co.nstauthority.offshoresafetydirective.systemofrecord.actuator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentSnsService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;

class AppointmentConfirmedMessageActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT =
      "/actuator/appointment-confirmed-message/%s";

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

    var actuatorUrl = PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id());

    mockMvc.perform(
        post(actuatorUrl).with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isOk());

    verify(appointmentSnsService).publishAppointmentCreatedSnsMessage(appointment);
  }

  @Test
  void publishAppointmentConfirmedMessage_noAppointment_thenBadRequest() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.empty());

    var actuatorUrl = PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id());

    mockMvc.perform(
        post(actuatorUrl).with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isBadRequest());

    verify(appointmentSnsService, never()).publishAppointmentCreatedSnsMessage(any());
  }

  @SecurityTest
  void publishAppointmentConfirmedMessage_notAuthorised() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var actuatorUrl = PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id());

    mockMvc.perform(post(actuatorUrl).with(httpBasic("admin", "invalid-password")))
        .andExpect(status().isUnauthorized());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }
}