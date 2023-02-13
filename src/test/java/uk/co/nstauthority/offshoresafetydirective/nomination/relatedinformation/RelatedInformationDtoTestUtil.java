package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class RelatedInformationDtoTestUtil {

  private RelatedInformationDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private RelatedToPearsApplications relatedToPearsApplications = new RelatedToPearsApplications(false, null);

    private Builder() {
    }

    public Builder withRelatedApplicationReference(String pearsReferences) {
      this.relatedToPearsApplications = new RelatedToPearsApplications(
          relatedToPearsApplications.related(),
          pearsReferences
      );
      return this;
    }

    public Builder withRelatedToPearsApplications(RelatedToPearsApplications relatedToPearsApplications) {
      this.relatedToPearsApplications = relatedToPearsApplications;
      return this;
    }

    public RelatedInformationDto build() {
      return new RelatedInformationDto(relatedToPearsApplications);
    }

  }

}