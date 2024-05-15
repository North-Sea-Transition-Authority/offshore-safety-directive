package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

class FieldDtoTest {

  @Test
  void fromPortalField_verifyMappings() {

    var field = FieldTestUtil.builder()
        .withId(100)
        .withName("field-name")
        .withStatus(FieldStatus.STATUS100)
        .build();

    var resultingFieldDto = FieldDto.fromPortalField(field);

    PropertyObjectAssert.thenAssertThat(resultingFieldDto)
        .hasFieldOrPropertyWithValue("fieldId", new FieldId(100))
        .hasFieldOrPropertyWithValue("name", "field-name")
        .hasFieldOrPropertyWithValue("status", FieldStatus.STATUS100)
        .hasAssertedAllProperties();
  }

  @ParameterizedTest
  @EnumSource(value = FieldStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "STATUS9999")
  void isActive_whenNonDeletedStatus_thenTrue(FieldStatus fieldStatus) {

    var fieldDto = FieldDtoTestUtil.builder()
        .withStatus(fieldStatus)
        .build();

    assertTrue(fieldDto.isActive());
  }

  @Test
  void isActive_whenDeletedStatus_thenFalse() {

    var fieldDto = FieldDtoTestUtil.builder()
        .withStatus(FieldStatus.STATUS9999)
        .build();

    assertFalse(fieldDto.isActive());
  }

}