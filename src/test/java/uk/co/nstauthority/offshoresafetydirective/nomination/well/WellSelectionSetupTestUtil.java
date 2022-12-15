package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class WellSelectionSetupTestUtil {

  private WellSelectionSetupTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static WellSelectionSetupBuilder builder() {
    return new WellSelectionSetupBuilder();
  }

  public static class WellSelectionSetupBuilder {
    private Integer id = 100;
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private WellSelectionType wellSelectionType = WellSelectionType.SPECIFIC_WELLS;

    public WellSelectionSetupBuilder withId(Integer id) {
      this.id = id;
      return this;
    }

    public WellSelectionSetupBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public WellSelectionSetupBuilder withWellSelectionType(WellSelectionType wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
      return this;
    }

    public WellSelectionSetup build() {
      var wellSelectionSetup = new WellSelectionSetup(
          nominationDetail,
          wellSelectionType
      );
      wellSelectionSetup.setId(id);
      return wellSelectionSetup;
    }
  }
}
