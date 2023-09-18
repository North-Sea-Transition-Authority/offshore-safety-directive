package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class AppointmentUpdateServiceTest {

  @Mock
  private AppointmentRepository appointmentRepository;

  @InjectMocks
  private AppointmentUpdateService appointmentUpdateService;

  @Test
  void updateAppointment_whenDeemedAppointment() {

    var appointedOperatorId = 1001;
    var appointmentType = AppointmentType.DEEMED;
    var appointmentId = UUID.randomUUID();

    var fromDate = LocalDate.now().minusDays(1);
    var toDate = LocalDate.now();

    var offlineNominationReference = "OFFLINE/REF/1";
    var onlineNominationReference = new NominationId(UUID.randomUUID());

    var appointment = mock(Appointment.class);
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId)
        .withAppointedOperatorId(appointedOperatorId)
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate(fromDate)
        .withAppointmentToDate(toDate)
        .withLegacyNominationReference(offlineNominationReference)
        .withNominationId(onlineNominationReference)
        .build();

    when(appointmentRepository.findById(appointmentId))
        .thenReturn(Optional.of(appointment));

    appointmentUpdateService.updateAppointment(appointmentDto);

    var captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(appointment);

    verify(appointment).setAppointedPortalOperatorId(appointedOperatorId);
    verify(appointment).setAppointmentType(appointmentType);
    verify(appointment).setResponsibleFromDate(fromDate);
    verify(appointment).setResponsibleToDate(toDate);
    verify(appointment).setCreatedByLegacyNominationReference(null);
    verify(appointment).setCreatedByNominationId(null);
    verifyNoMoreInteractions(appointment);
  }

  @Test
  void updateAppointment_whenOnlineAppointment() {
    var appointedOperatorId = 1001;
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var appointmentId = UUID.randomUUID();

    var fromDate = LocalDate.now().minusDays(1);
    var toDate = LocalDate.now();

    var offlineNominationReference = "OFFLINE/REF/1";
    var onlineNominationReference = new NominationId(UUID.randomUUID());

    var appointment = mock(Appointment.class);
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId)
        .withAppointedOperatorId(appointedOperatorId)
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate(fromDate)
        .withAppointmentToDate(toDate)
        .withLegacyNominationReference(offlineNominationReference)
        .withNominationId(onlineNominationReference)
        .build();

    when(appointmentRepository.findById(appointmentId))
        .thenReturn(Optional.of(appointment));

    appointmentUpdateService.updateAppointment(appointmentDto);

    var captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(appointment);

    verify(appointment).setAppointedPortalOperatorId(appointedOperatorId);
    verify(appointment).setAppointmentType(appointmentType);
    verify(appointment).setResponsibleFromDate(fromDate);
    verify(appointment).setResponsibleToDate(toDate);
    verify(appointment).setCreatedByLegacyNominationReference(null);
    verify(appointment).setCreatedByNominationId(onlineNominationReference.id());
    verifyNoMoreInteractions(appointment);
  }

  @Test
  void updateAppointment_whenOfflineAppointment() {
    var appointedOperatorId = 1001;
    var appointmentType = AppointmentType.OFFLINE_NOMINATION;
    var appointmentId = UUID.randomUUID();

    var fromDate = LocalDate.now().minusDays(1);
    var toDate = LocalDate.now();

    var offlineNominationReference = "OFFLINE/REF/1";
    var onlineNominationReference = new NominationId(UUID.randomUUID());

    var appointment = mock(Appointment.class);
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId)
        .withAppointedOperatorId(appointedOperatorId)
        .withAppointmentType(appointmentType)
        .withAppointmentFromDate(fromDate)
        .withAppointmentToDate(toDate)
        .withLegacyNominationReference(offlineNominationReference)
        .withNominationId(onlineNominationReference)
        .build();

    when(appointmentRepository.findById(appointmentId))
        .thenReturn(Optional.of(appointment));

    appointmentUpdateService.updateAppointment(appointmentDto);

    var captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(appointment);

    verify(appointment).setAppointedPortalOperatorId(appointedOperatorId);
    verify(appointment).setAppointmentType(appointmentType);
    verify(appointment).setResponsibleFromDate(fromDate);
    verify(appointment).setResponsibleToDate(toDate);
    verify(appointment).setCreatedByLegacyNominationReference(offlineNominationReference);
    verify(appointment).setCreatedByNominationId(null);
    verifyNoMoreInteractions(appointment);
  }

  @Test
  void updateAppointment_whenToDateIsNull() {
    var appointmentId = UUID.randomUUID();

    var appointment = mock(Appointment.class);
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId)
        .withAppointmentToDate((AppointmentToDate) null)
        .build();

    when(appointmentRepository.findById(appointmentId))
        .thenReturn(Optional.of(appointment));

    appointmentUpdateService.updateAppointment(appointmentDto);

    var captor = ArgumentCaptor.forClass(Appointment.class);
    verify(appointmentRepository).save(captor.capture());

    assertThat(captor.getValue()).isEqualTo(appointment);
    verify(appointment).setResponsibleToDate(null);
  }

  @Test
  void updateAppointment_whenNotFound_verifyThrows() {

    var appointmentId = UUID.randomUUID();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(appointmentId)
        .build();

    when(appointmentRepository.findById(appointmentId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> appointmentUpdateService.updateAppointment(appointmentDto))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No appointment found with id [%s]".formatted(appointmentId));
  }
}