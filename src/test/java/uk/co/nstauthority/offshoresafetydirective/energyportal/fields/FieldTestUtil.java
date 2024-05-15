package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldGeographicArea;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.fivium.energyportalapi.generated.types.FieldSubType;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class FieldTestUtil {

  private FieldTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int fieldId;
    private String fieldName;
    private FieldShore shore;
    private FieldStatus status;
    private FieldSubType subType;
    private FieldGeographicArea geographicArea;

    private Builder() {
      fieldId = 1000;
      fieldName = "Field name";
      shore = FieldShore.OFFSHORE;
      status = FieldStatus.STATUS100;
      subType = FieldSubType.SUB_AREA;
      geographicArea = FieldGeographicArea.CNS;
    }

    public Builder withId(int fieldId) {
      this.fieldId = fieldId;
      return this;
    }

    public Builder withName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public Builder withShoreType(FieldShore shore) {
      this.shore = shore;
      return this;
    }

    public Builder withStatus(FieldStatus status) {
      this.status = status;
      return this;
    }

    public Builder withSubType(FieldSubType subType) {
      this.subType = subType;
      return this;
    }

    public Builder withGeographicArea(FieldGeographicArea geographicArea) {
      this.geographicArea = geographicArea;
      return this;
    }

    public Field build() {
      return Field.newBuilder()
          .fieldId(fieldId)
          .fieldName(fieldName)
          .shore(shore)
          .status(status)
          .subType(subType)
          .geographicArea(geographicArea)
          .build();
    }
  }
}
