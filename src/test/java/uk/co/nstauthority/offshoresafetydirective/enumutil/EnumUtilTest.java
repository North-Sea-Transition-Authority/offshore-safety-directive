package uk.co.nstauthority.offshoresafetydirective.enumutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class EnumUtilTest {

  @Test
  void getEnumNames_fromCollection() {
    var enumNames = EnumUtil.getEnumNames(List.of(TestEnum.VALUE_A, TestEnum.VALUE_B));
    assertThat(enumNames)
        .containsExactly("VALUE_A", "VALUE_B");
  }

  @Test
  void getEnumNames_fromEnum() {
    var enumNames = EnumUtil.getEnumNames(TestEnum.class);
    assertThat(enumNames)
        .containsExactly("VALUE_A", "VALUE_B");
  }

  @Test
  void getEnumValue() {
    var result = EnumUtil.getEnumValue(TestEnum.class, "VALUE_A");
    assertThat(result).isEqualTo(TestEnum.VALUE_A);
  }

  private enum TestEnum {

    VALUE_A,
    VALUE_B

  }
}