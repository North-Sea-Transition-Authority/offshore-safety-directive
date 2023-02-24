package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

  @Mock
  private AppointmentRepository appointmentRepository;

  @Mock
  private NomineeDetailAccessService nomineeDetailAccessService;

  @InjectMocks
  private AppointmentService appointmentService;

  @Test
  void addAppointments_whenExistingAppointments_verifyEnded() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

    var existingAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withResponsibleFromDate(confirmationDate.minusDays(1))
        .withResponsibleToDate(null)
        .build();

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNull(List.of(asset)))
        .thenReturn(List.of(existingAppointment));

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetailDto));

    var appointments = appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(asset));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentListCaptor = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository).saveAll(appointmentListCaptor.capture());

    assertThat(appointmentListCaptor.getValue())
        .extracting(
            Appointment::getAsset,
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate
        )
        .containsExactly(
            tuple(asset, confirmationDate.minusDays(1), confirmationDate),
            tuple(asset, confirmationDate, null)
        );
  }

  @Test
  void addAppointments_whenNoExistingAppointments_verifyNoExistingUpdated() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNull(List.of(asset)))
        .thenReturn(List.of());

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetailDto));

    var appointments = appointmentService.addAppointments(nominationDetail, confirmationDate, List.of(asset));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentListCaptor = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository, times(1)).saveAll(appointmentListCaptor.capture());

    var savedNewAppointments = appointmentListCaptor.getAllValues().get(0);

    assertThat(savedNewAppointments)
        .extracting(
            Appointment::getAsset,
            Appointment::getResponsibleFromDate
        )
        .containsExactly(tuple(asset, confirmationDate));

    assertThat(appointments).isEqualTo(appointmentListCaptor.getValue());
  }

  @Test
  void addAppointments_whenNoNomineeDetailDto_verifyError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var confirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var assetList = List.of(asset);

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> appointmentService.addAppointments(nominationDetail, confirmationDate, assetList))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to get NomineeDetailDto for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId()
        ));
  }
}