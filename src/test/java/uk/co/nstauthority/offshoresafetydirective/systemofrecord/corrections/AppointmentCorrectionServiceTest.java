package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionServiceTest {

  @Mock
  private AppointmentUpdateService appointmentUpdateService;

  @InjectMocks
  private AppointmentCorrectionService appointmentCorrectionService;

  @Test
  void updateCorrection() {

    var originalAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .withPortalAssetId("portal/asset/id")
        .withAppointedOperatorId(456)
        .withAppointmentFromDate(LocalDate.now().minusDays(1))
        .withAppointmentToDate(LocalDate.now().plusDays(2))
        .withAppointmentCreatedDatetime(Instant.now())
        .withAppointmentType(AppointmentType.NOMINATED)
        .withLegacyNominationReference("legacy/ref")
        .withNominationId(new NominationId(789))
        .build();

    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(123);

    appointmentCorrectionService.updateCorrection(originalAppointmentDto, form);

    var captor = ArgumentCaptor.forClass(AppointmentDto.class);
    verify(appointmentUpdateService).updateAppointment(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("appointmentId", originalAppointmentDto.appointmentId())
        .hasFieldOrPropertyWithValue("portalAssetId", originalAppointmentDto.portalAssetId())
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(form.getAppointedOperatorId().toString())
        )
        .hasFieldOrPropertyWithValue("appointmentFromDate", originalAppointmentDto.appointmentFromDate())
        .hasFieldOrPropertyWithValue("appointmentToDate", originalAppointmentDto.appointmentToDate())
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", originalAppointmentDto.appointmentCreatedDate())
        .hasFieldOrPropertyWithValue("appointmentType", originalAppointmentDto.appointmentType())
        .hasFieldOrPropertyWithValue("legacyNominationReference", originalAppointmentDto.legacyNominationReference())
        .hasFieldOrPropertyWithValue("nominationId", originalAppointmentDto.nominationId())
        .hasAssertedAllProperties();
  }

  @Test
  void getForm_assertMappings() {

    var appointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(500)
        .build();

    var resultingForm = appointmentCorrectionService.getForm(appointment);

    PropertyObjectAssert.thenAssertThat(resultingForm)
        .hasFieldOrPropertyWithValue("appointedOperatorId", 500)
        .hasAssertedAllProperties();
  }
}