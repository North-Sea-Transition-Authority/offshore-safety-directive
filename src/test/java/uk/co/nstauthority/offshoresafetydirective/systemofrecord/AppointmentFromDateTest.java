package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AppointmentFromDateTest {

  @Test
  void formattedValue_whenDateIsNull_thenEmptyStringReturned() {
    var resultingFormattedDate = new AppointmentFromDate(null).formattedValue();
    assertThat(resultingFormattedDate).isEmpty();
  }

  @Test
  void formattedValue_whenDateIsNotNull_thenLongDateFormatReturned() {

    var appointmentFromDate = LocalDate.of(2023, 2, 24);

    var resultingFormattedDate = new AppointmentFromDate(appointmentFromDate).formattedValue();
    assertThat(resultingFormattedDate).isEqualTo("24 February 2023");
  }

}