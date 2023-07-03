package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

  private static final Instant FIXED_INSTANT = Instant.now();

  @Mock
  private AppointmentRepository appointmentRepository;

  @Mock
  private NomineeDetailAccessService nomineeDetailAccessService;

  private AppointmentService appointmentService;

  @BeforeEach
  void setup() {

    var clock = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());

    appointmentService = new AppointmentService(
        appointmentRepository,
        nomineeDetailAccessService,
        clock
    );
  }

  @Test
  void addAppointments_whenExistingAppointments_verifyEnded() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var newAppointmentConfirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

    var existingAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withResponsibleFromDate(newAppointmentConfirmationDate.minusDays(1))
        .withResponsibleToDate(null)
        .build();

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNull(List.of(asset)))
        .thenReturn(List.of(existingAppointment));

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetailDto));

    var appointments = appointmentService.addAppointments(nominationDetail, newAppointmentConfirmationDate, List.of(asset));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentsPersisted = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository).saveAll(appointmentsPersisted.capture());

    // then the existing appointments for the asset are ended
    // and the new appointment created
    assertThat(appointmentsPersisted.getValue())
        .extracting(
            Appointment::getAsset,
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate,
            Appointment::getCreatedByNominationId,
            Appointment::getAppointmentType,
            Appointment::getAppointedPortalOperatorId,
            Appointment::getCreatedDatetime
        )
        .containsExactly(
            tuple(
                asset,
                existingAppointment.getResponsibleFromDate(),
                newAppointmentConfirmationDate,
                existingAppointment.getCreatedByNominationId(),
                existingAppointment.getAppointmentType(),
                existingAppointment.getAppointedPortalOperatorId(),
                existingAppointment.getCreatedDatetime()
            ),
            tuple(
                asset,
                newAppointmentConfirmationDate,
                null,
                nominationDetail.getNomination().getId(),
                AppointmentType.ONLINE_NOMINATION,
                nomineeDetailDto.nominatedOrganisationId().id(),
                FIXED_INSTANT
            )
        );

    // and only the new appointment is returned
    assertThat(appointments)
        .extracting(
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate
        )
        .containsExactly(
            tuple(newAppointmentConfirmationDate, null)
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
    ArgumentCaptor<List<Appointment>> appointmentsPersisted = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository, times(1)).saveAll(appointmentsPersisted.capture());

    var savedNewAppointments = appointmentsPersisted.getAllValues().get(0);

    assertThat(savedNewAppointments)
        .extracting(
            Appointment::getAsset,
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate,
            Appointment::getCreatedByNominationId,
            Appointment::getAppointmentType,
            Appointment::getAppointedPortalOperatorId,
            Appointment::getCreatedDatetime
        )
        .containsExactly(
            tuple(
                asset,
                confirmationDate,
                null,
                nominationDetail.getNomination().getId(),
                AppointmentType.ONLINE_NOMINATION,
                nomineeDetailDto.nominatedOrganisationId().id(),
                FIXED_INSTANT
            )
        );

    assertThat(appointments).isEqualTo(appointmentsPersisted.getValue());
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