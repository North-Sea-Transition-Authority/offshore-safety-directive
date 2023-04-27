package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class RelatedInformationTestUtil {

  RelatedInformationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 150;
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean relatedToAnyFields = null;

    private Boolean relatedToLicenceApplications = true;

    private String relatedLicenceApplications = "related licence applications";

    private Boolean relatedToWellApplications = true;

    private String relatedWellApplications = "related well applications";

    private Builder() {
    }

    public Builder withId(Integer id) {
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

    public Builder withRelatedToLicenceApplications(Boolean relatedToLicenceApplications) {
      this.relatedToLicenceApplications = relatedToLicenceApplications;
      return this;
    }

    public Builder withRelatedLicenceApplications(String relatedLicenceApplications) {
      this.relatedLicenceApplications = relatedLicenceApplications;
      return this;
    }

    public Builder withRelatedToWellApplications(Boolean relatedToWellApplications) {
      this.relatedToWellApplications = relatedToWellApplications;
      return this;
    }

    public Builder withRelatedWellApplications(String relatedWellApplications) {
      this.relatedWellApplications = relatedWellApplications;
      return this;
    }

    public RelatedInformation build() {
      var relatedInformation = new RelatedInformation();
      relatedInformation.setId(id);
      relatedInformation.setNominationDetail(nominationDetail);
      relatedInformation.setRelatedToFields(relatedToAnyFields);
      relatedInformation.setRelatedToLicenceApplications(relatedToLicenceApplications);
      relatedInformation.setRelatedLicenceApplications(relatedLicenceApplications);
      relatedInformation.setRelatedToWellApplications(relatedToWellApplications);
      relatedInformation.setRelatedWellApplications(relatedWellApplications);
      return relatedInformation;
    }

  }

}
