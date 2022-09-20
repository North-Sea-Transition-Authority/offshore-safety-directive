package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class WellSelectionSetupTestUtil {

  private WellSelectionSetupTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static WellSelectionSetupBuilder builder() {
    return new WellSelectionSetupBuilder();
  }

  static class WellSelectionSetupBuilder {
    private int id = 100;
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private WellSelectionType wellSelectionType = WellSelectionType.SPECIFIC_WELLS;

    WellSelectionSetupBuilder withId(int id) {
      this.id = id;
      return this;
    }

    WellSelectionSetupBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    WellSelectionSetupBuilder withWellSelectionType(WellSelectionType wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
      return this;
    }

    WellSelectionSetup build() {
      var wellSelectionSetup = new WellSelectionSetup(
          nominationDetail,
          wellSelectionType
      );
      wellSelectionSetup.setId(id);
      return wellSelectionSetup;
    }
  }
}
