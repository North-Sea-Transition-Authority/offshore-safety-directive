package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppointmentCorrectionHistoryViewTest {

  @Test
  void fromAppointmentCorrection_verifyMappings() {

    var createdOn = Instant.now();
    var correctionId = UUID.randomUUID();

    var appointmentCorrection = AppointmentCorrectionTestUtil.builder()
        .withUuid(correctionId)
        .withReasonForCorrection("reason for correction")
        .withCreatedTimestamp(createdOn)
        .build();

    var resultingHistoryItem = AppointmentCorrectionHistoryView.fromAppointmentCorrection(
        appointmentCorrection,
        "Created by user"
    );

    assertThat(resultingHistoryItem)
        .extracting(
            AppointmentCorrectionHistoryView::correctionId,
            AppointmentCorrectionHistoryView::reason,
            AppointmentCorrectionHistoryView::createdBy,
            AppointmentCorrectionHistoryView::createdInstant
        )
        .containsExactly(
            correctionId,
            "reason for correction",
            "Created by user",
            createdOn
        );
  }

  @Test
  void formattedCreatedDatetime_verifyFormattedDate() {

    var createdOn = LocalDateTime.of(2022, Month.JANUARY, 16, 17, 5, 48)
        .toInstant(ZoneOffset.UTC);

    var historyItem = AppointmentCorrectionHistoryViewTestUtil.builder()
        .withCreatedInstant(createdOn)
        .build();

    assertThat(historyItem.formattedCreatedDatetime()).isEqualTo("16 January 2022 17:05");
  }

}