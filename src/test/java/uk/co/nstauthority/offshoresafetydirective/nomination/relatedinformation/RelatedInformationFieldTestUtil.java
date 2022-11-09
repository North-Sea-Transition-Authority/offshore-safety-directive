package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class RelatedInformationFieldTestUtil {

  public RelatedInformationFieldTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int id = 1111;
    private RelatedInformation relatedInformation = RelatedInformationTestUtil.builder().build();
    private int fieldId = 1010;
    private String fieldName = "field name";

    private Builder() {
    }

    public Builder withId(int id) {
      this.id = id;
      return this;
    }

    public Builder withRelatedInformation(RelatedInformation relatedInformation) {
      this.relatedInformation = relatedInformation;
      return this;
    }

    public Builder withFieldId(int fieldId) {
      this.fieldId = fieldId;
      return this;
    }

    public Builder withFieldName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public RelatedInformationField build() {
      var field = new RelatedInformationField();
      field.setId(id);
      field.setRelatedInformation(relatedInformation);
      field.setFieldId(fieldId);
      field.setFieldName(fieldName);
      return field;
    }
  }
}
