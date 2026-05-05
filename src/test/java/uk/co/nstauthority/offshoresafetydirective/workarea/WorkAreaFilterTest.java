package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

class WorkAreaFilterTest {

  @Test
  void defaultFilter_canSeeDrafts() {
    var workAreaFilter = WorkAreaFilter.defaultFilter(true);

    assertThat(workAreaFilter.nominationStatuses())
        .containsExactlyInAnyOrder(
            NominationStatus.DRAFT,
            NominationStatus.SUBMITTED,
            NominationStatus.AWAITING_CONFIRMATION
        );
  }

  @Test
  void defaultFilter_cannotSeeDrafts() {
    var workAreaFilter = WorkAreaFilter.defaultFilter(false);

    assertThat(workAreaFilter.nominationStatuses())
        .containsExactlyInAnyOrder(
            NominationStatus.SUBMITTED,
            NominationStatus.AWAITING_CONFIRMATION
        );
  }

  @Test
  void fromForm() {
    var form = new WorkAreaFilterForm();
    form.setNominationStatuses(Set.of(NominationStatus.DRAFT, NominationStatus.APPOINTED));

    var workAreaFilter = WorkAreaFilter.fromForm(form);

    assertThat(workAreaFilter.nominationStatuses())
        .containsExactlyInAnyOrder(NominationStatus.DRAFT, NominationStatus.APPOINTED);
  }
}
