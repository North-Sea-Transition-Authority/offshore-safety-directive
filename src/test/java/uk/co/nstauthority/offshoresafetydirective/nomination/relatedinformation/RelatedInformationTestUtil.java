package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class RelatedInformationTestUtil {

  RelatedInformationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private int id = 150;
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean relatedToAnyFields = null;

    private Boolean relatedToLicenceApplications = true;

    private String relatedLicenceApplications = "related licence applications";

    private Boolean relatedToWellApplications = true;

    private String relatedWellApplications = "related well applications";

    private Builder() {
    }

    Builder withId(int id) {
      this.id = id;
      return this;
    }

    Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    Builder withRelationToAnyField(Boolean related) {
      this.relatedToAnyFields = related;
      return this;
    }

    Builder withRelatedToLicenceApplications(Boolean relatedToLicenceApplications) {
      this.relatedToLicenceApplications = relatedToLicenceApplications;
      return this;
    }

    Builder withRelatedLicenceApplications(String relatedLicenceApplications) {
      this.relatedLicenceApplications = relatedLicenceApplications;
      return this;
    }

    Builder withRelatedToWellApplications(Boolean relatedToWellApplications) {
      this.relatedToWellApplications = relatedToWellApplications;
      return this;
    }

    Builder withRelatedWellApplications(String relatedWellApplications) {
      this.relatedWellApplications = relatedWellApplications;
      return this;
    }

    RelatedInformation build() {
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
