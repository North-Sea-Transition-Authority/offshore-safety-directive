package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class RelatedInformationTestUtil {

  public RelatedInformationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int id = 150;
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean relatedToAnyFields = null;

    private Builder() {
    }

    public Builder withId(int id) {
      this.id = id;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withRelationToAnyField(Boolean related) {
      this.relatedToAnyFields = related;
      return this;
    }

    public RelatedInformation build() {
      var relatedInformation = new RelatedInformation();
      relatedInformation.setId(id);
      relatedInformation.setNominationDetail(nominationDetail);
      relatedInformation.setRelatedToFields(relatedToAnyFields);

      return relatedInformation;
    }

  }

}
