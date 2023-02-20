package uk.co.nstauthority.offshoresafetydirective.stringutil;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilTest {

  @Test
  void abbreviate_whenCharacterCountIsLess_assertSameString() {
    var value = "testing";
    var maxLength = value.length() + 10;
    var result = StringUtil.abbreviate(value, maxLength);
    assertThat(result).isEqualTo(value);
  }

  @Test
  void abbreviate_whenCharacterCountIsSame_assertSameString() {
    var value = "testing";
    var maxLength = value.length();
    var result = StringUtil.abbreviate(value, maxLength);
    assertThat(result).isEqualTo(value);
  }

  @Test
  void abbreviate_whenCharacterCountIsMore_assertAbbreviatedString() {
    var value = "testing";
    var maxLength = value.length() - 1;
    var result = StringUtil.abbreviate(value, maxLength);
    assertThat(result).isEqualTo(value.substring(0, maxLength - 3) + StringUtil.ELLIPSIS_CHARACTER);
  }
}