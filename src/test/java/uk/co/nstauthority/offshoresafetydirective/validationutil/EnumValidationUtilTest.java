package uk.co.nstauthority.offshoresafetydirective.validationutil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnumValidationUtilTest {

  @Test
  void isValidEnumValue_whenValidEnumValue_thenTrue() {
    assertTrue(EnumValidationUtil.isValidEnumValue(TesterEnum.class, TesterEnum.VALUE_A.name()));
  }

  @Test
  void isValidEnumValue_whenInvalidEnumValue_thenFalse() {
    assertFalse(EnumValidationUtil.isValidEnumValue(TesterEnum.class, "VALUE_F"));
  }

  private enum TesterEnum {
    VALUE_A,
    VALUE_B,
    VALUE_C
  }
}