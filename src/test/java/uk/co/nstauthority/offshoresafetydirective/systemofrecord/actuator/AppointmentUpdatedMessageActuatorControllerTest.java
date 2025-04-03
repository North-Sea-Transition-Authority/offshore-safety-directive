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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentSnsService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;

class AppointmentUpdatedMessageActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT =
      "/actuator/appointment-updated-message/%s";

  @MockitoBean
  private AppointmentAccessService appointmentAccessService;

  @MockitoBean
  private AppointmentSnsService appointmentSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @Test
  void whenAppointmentNotFound() throws Exception {

    var appointmentId = UUID.randomUUID();

    given(appointmentAccessService.getAppointmentByStatus(new AppointmentId(appointmentId), AppointmentStatus.EXTANT))
        .willReturn(Optional.empty());

    var actuatorUrl = PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(
        post(actuatorUrl).with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isBadRequest());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }

  @Test
  void whenAppointmentEnded() throws Exception {

    var appointmentId = UUID.randomUUID();

    var endedAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .withResponsibleToDate(LocalDate.now())
        .build();

    given(appointmentAccessService.getAppointmentByStatus(new AppointmentId(appointmentId), AppointmentStatus.EXTANT))
        .willReturn(Optional.of(endedAppointment));

    var actuatorUrl = PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(
        post(actuatorUrl).with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isBadRequest());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }

  @Test
  void whenAppointmentUpdated() throws Exception {

    var appointmentId = UUID.randomUUID();

    var activeAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .withResponsibleToDate(null)
        .build();

    given(appointmentAccessService.getAppointmentByStatus(new AppointmentId(appointmentId), AppointmentStatus.EXTANT))
        .willReturn(Optional.of(activeAppointment));

    var actuatorUrl = PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(
        post(actuatorUrl)
            .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, "correlation-id")
            .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
    )
        .andExpect(status().isOk());

    then(appointmentSnsService)
        .should()
        .publishAppointmentUpdatedSnsMessage(activeAppointment, "correlation-id");
  }

  @SecurityTest
  void whenNotUnauthorized() throws Exception {

    var appointmentId = new AppointmentId(UUID.randomUUID());

    var actuatorUrl = PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId);

    mockMvc.perform(post(actuatorUrl).with(httpBasic("admin", "invalid-password")))
        .andExpect(status().isUnauthorized());

    then(appointmentSnsService)
        .should(never())
        .publishAppointmentUpdatedSnsMessage(any(), anyString());
  }

}