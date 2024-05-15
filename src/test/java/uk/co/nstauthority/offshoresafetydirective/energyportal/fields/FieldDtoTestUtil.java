package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class FieldDtoTestUtil {

  private FieldDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private FieldId id = new FieldId(100);

    private String name = "field-name";

    private FieldStatus status = FieldStatus.STATUS500;

    private Builder() {
    }

    public Builder withId(Integer id) {
      return withId(new FieldId(id));
    }

    public Builder withId(FieldId id) {
      this.id = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withStatus(FieldStatus status) {
      this.status = status;
      return this;
    }

    public FieldDto build() {
      return new FieldDto(id, name, status);
    }
  }
}
