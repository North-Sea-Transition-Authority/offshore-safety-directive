package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentAccessServiceTest {

  @Mock
  private AppointmentRepository appointmentRepository;

  @InjectMocks
  private AppointmentAccessService appointmentAccessService;

  @Test
  void getAppointmentsForAsset_whenNoAppointments_themEmptyListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    given(appointmentRepository.findAllByAsset_id(assetId.id()))
        .willReturn(Collections.emptyList());

    var resultingAppointments = appointmentAccessService.getAppointmentsForAsset(assetId);

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void getAppointmentsForAsset_whenAppointments_thenPopulatedListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    var expectedAppointment = AppointmentTestUtil.builder().build();

    given(appointmentRepository.findAllByAsset_id(assetId.id()))
        .willReturn(List.of(expectedAppointment));

    var resultingAppointments = appointmentAccessService.getAppointmentsForAsset(assetId);

    assertThat(resultingAppointments)
        .extracting(
            appointmentDto -> appointmentDto.appointmentId().id(),
            appointmentDto -> appointmentDto.appointedOperatorId().id(),
            appointmentDto -> appointmentDto.appointmentFromDate().value(),
            appointmentDto -> appointmentDto.appointmentToDate().value(),
            AppointmentDto::appointmentCreatedDate,
            AppointmentDto::assetDto
        )
        .containsExactly(
            tuple(
                expectedAppointment.getId(),
                String.valueOf(expectedAppointment.getAppointedPortalOperatorId()),
                expectedAppointment.getResponsibleFromDate(),
                expectedAppointment.getResponsibleToDate(),
                expectedAppointment.getCreatedDatetime(),
                AssetDto.fromAsset(expectedAppointment.getAsset())
            )
        );
  }

  @Test
  void getAppointment_appointmentFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder().withId(appointmentId.id()).build();

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.of(appointment));

    assertThat(appointmentAccessService.getAppointment(appointmentId)).contains(appointment);
  }

  @Test
  void getAppointment_appointmentNotFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.empty());

    assertThat(appointmentAccessService.getAppointment(appointmentId)).isEmpty();
  }

  @Test
  void findAppointmentDtoById_appointmentFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var asset = AssetTestUtil.builder().build();

    var appointedOperatorId = 100;
    var fromDate = LocalDate.now();
    var toDate = LocalDate.now();
    var appointmentType = AppointmentType.NOMINATED;
    var nominationId = 200;
    var createdDatetime = Instant.now();
    var legacyReference = "legacy reference";

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withAsset(asset)
        .withAppointedPortalOperatorId(appointedOperatorId)
        .withResponsibleFromDate(fromDate)
        .withResponsibleToDate(toDate)
        .withAppointmentType(appointmentType)
        .withCreatedByNominationId(nominationId)
        .withCreatedByLegacyNominationReference(legacyReference)
        .withCreatedByAppointmentId(appointmentId.id())
        .withCreatedDatetime(createdDatetime)
        .build();

    when(appointmentRepository.findById(appointmentId.id()))
        .thenReturn(Optional.of(appointment));

    var result = appointmentAccessService.findAppointmentDtoById(appointmentId);

    assertThat(result).isNotEmpty();

    PropertyObjectAssert.thenAssertThat(result.get())
        .hasFieldOrPropertyWithValue("appointmentId", appointmentId)
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(String.valueOf(appointedOperatorId))
        )
        .hasFieldOrPropertyWithValue("appointmentFromDate", new AppointmentFromDate(fromDate))
        .hasFieldOrPropertyWithValue("appointmentToDate", new AppointmentToDate(toDate))
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", createdDatetime)
        .hasFieldOrPropertyWithValue("appointmentType", appointmentType)
        .hasFieldOrPropertyWithValue("legacyNominationReference", legacyReference)
        .hasFieldOrPropertyWithValue("nominationId", new NominationId(nominationId))
        .hasFieldOrPropertyWithValue("assetDto", AssetDto.fromAsset(asset))
        .hasAssertedAllProperties();
  }

  @Test
  void findAppointmentDtoById_appointmentNotFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentRepository.findById(appointmentId.id()))
        .thenReturn(Optional.empty());

    var result = appointmentAccessService.findAppointmentDtoById(appointmentId);

    assertThat(result).isEmpty();
  }

}
