package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

class AppointmentSearchItemDtoTest {

  @Test
  void displayableAppointmentDate_verifyLongDateFormat() {

    var expectedAppointmentDate = LocalDate.of(2022, Month.JANUARY, 16);

    var appointmentSearchItemDto = AppointmentSearchItemDtoTestUtil.builder()
        .withAppointmentDate(expectedAppointmentDate)
        .build();

    assertThat(appointmentSearchItemDto.displayableAppointmentDate()).isEqualTo("16 January 2022");
  }

  @Test
  void displayableAppointmentDate_whenNullThenEmptyString() {

    var appointmentSearchItemDto = AppointmentSearchItemDtoTestUtil.builder()
        .withAppointmentDate(null)
        .build();

    assertThat(appointmentSearchItemDto.displayableAppointmentDate()).isEmpty();
  }

}