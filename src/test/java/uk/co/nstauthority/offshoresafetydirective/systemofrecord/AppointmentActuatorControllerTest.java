package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.nstauthority.offshoresafetydirective.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractActuatorControllerTest;

class AppointmentActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT =
      "/actuator/appointments/publish-epmq-message/appointment/%s/confirmed";

  private static final String PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT =
      "/actuator/appointments/publish-epmq-message/appointment/%s/deleted";

  private static final String PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT =
      "/actuator/appointments/publish-epmq-message/appointment/%s/updated";

  private static final String CORRELATION_ID = "1";

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

    verify(appointmentSnsService).publishAppointmentCreatedSnsMessage(appointment);
  }

  @Test
  void publishAppointmentConfirmedMessage_noAppointment_thenBadRequest() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_CONFIRMED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword()))
        )
        .andExpect(status().isBadRequest());

    verify(appointmentSnsService, never()).publishAppointmentCreatedSnsMessage(any());
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

  @ParameterizedTest
  @EnumSource(value = AppointmentStatus.class, mode = EnumSource.Mode.INCLUDE, names = {"TERMINATED", "REMOVED"})
  void publishAppointmentDeletedMessage_validStatus(AppointmentStatus validStatus) throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withAppointmentStatus(validStatus)
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, CORRELATION_ID)
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())
        ))
        .andExpect(status().isOk());

    verify(appointmentSnsService).publishAppointmentDeletedSnsMessage(appointment.getId(), CORRELATION_ID);
  }

  @Test
  void publishAppointmentDeletedMessage_extantStatus_andEndDate() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .withResponsibleToDate(LocalDate.now())
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, CORRELATION_ID)
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())
                ))
        .andExpect(status().isOk());

    verify(appointmentSnsService).publishAppointmentDeletedSnsMessage(appointment.getId(), CORRELATION_ID);
  }

  @Test
  void publishAppointmentDeletedMessage_extantStatus_andNoEndDate() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .withResponsibleToDate(null)
        .build();

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.of(appointment));

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, CORRELATION_ID)
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())
                ))
        .andExpect(status().isBadRequest());

    verify(appointmentSnsService, never()).publishAppointmentDeletedSnsMessage(any(), any());
  }

  @Test
  void publishAppointmentDeletedMessage_noAppointment_thenBadRequest() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointment(appointmentId))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())))
        .andExpect(status().isBadRequest());

    verify(appointmentSnsService, never()).publishAppointmentDeletedSnsMessage(any(), any());
  }

  @SecurityTest
  void publishAppointmentDeletedMessage_notAuthorised() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_DELETED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", "invalidpassword"))
        )
        .andExpect(status().isUnauthorized());
  }

  @Test
  void publishAppointmentUpdatedMessage_withNoEndDate() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withResponsibleToDate(null)
        .build();

    when(appointmentAccessService.getAppointmentByStatus(appointmentId, AppointmentStatus.EXTANT))
        .thenReturn(Optional.of(appointment));

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, CORRELATION_ID)
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())
                ))
        .andExpect(status().isOk());

    verify(appointmentSnsService).publishAppointmentUpdatedSnsMessage(appointment, CORRELATION_ID);
  }

  @Test
  void publishAppointmentUpdatedMessage_withEndDate() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withResponsibleToDate(LocalDate.now())
        .build();

    when(appointmentAccessService.getAppointmentByStatus(appointmentId, AppointmentStatus.EXTANT))
        .thenReturn(Optional.of(appointment));

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .header(CorrelationIdUtil.HTTP_CORRELATION_ID_HEADER, CORRELATION_ID)
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())
                ))
        .andExpect(status().isBadRequest());

    verify(appointmentSnsService, never()).publishAppointmentDeletedSnsMessage(any(), any());
  }

  @Test
  void publishAppointmentUpdatedMessage_noAppointment_thenBadRequest() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.getAppointmentByStatus(appointmentId, AppointmentStatus.EXTANT))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())))
        .andExpect(status().isBadRequest());

    verify(appointmentSnsService, never()).publishAppointmentDeletedSnsMessage(any(), any());
  }

  @SecurityTest
  void publishAppointmentUpdatedMessage_notAuthorised() throws Exception {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    mockMvc
        .perform(
            post(PUBLISH_APPOINTMENT_UPDATED_MESSAGE_URL_FORMAT.formatted(appointmentId.id()))
                .with(httpBasic("admin", "invalidpassword"))
        )
        .andExpect(status().isUnauthorized());
  }
}
