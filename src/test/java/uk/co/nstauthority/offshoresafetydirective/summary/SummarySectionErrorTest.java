package uk.co.nstauthority.offshoresafetydirective.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SummarySectionErrorTest {

  @Test
  void createWithDefaultMessage() {
    var sectionName = "section name";
    var result = SummarySectionError.createWithDefaultMessage(sectionName);

    assertThat(result.errorMessage())
        .isEqualTo("There are problems with the %s section. Return to the task list to fix the problems."
            .formatted(sectionName));
  }
}