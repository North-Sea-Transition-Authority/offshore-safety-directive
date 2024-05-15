package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;

public record FieldDto(FieldId fieldId, String name, FieldStatus status) {

  static FieldDto fromPortalField(Field field) {
    return new FieldDto(new FieldId(field.getFieldId()), field.getFieldName(), field.getStatus());
  }

  public boolean isActive() {
    return !FieldStatus.STATUS9999.equals(status);
  }
}
