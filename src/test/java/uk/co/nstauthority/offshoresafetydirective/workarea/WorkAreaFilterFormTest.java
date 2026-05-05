package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

class WorkAreaFilterFormTest {

  @Test
  void fromFilter() {
    var statuses = Set.of(NominationStatus.DRAFT, NominationStatus.SUBMITTED);
    var workAreaFilter = new WorkAreaFilter(statuses);

    var form = WorkAreaFilterForm.fromFilter(workAreaFilter);

    assertThat(form.getNominationStatuses())
        .containsExactlyInAnyOrder(NominationStatus.DRAFT, NominationStatus.SUBMITTED);
  }
}
