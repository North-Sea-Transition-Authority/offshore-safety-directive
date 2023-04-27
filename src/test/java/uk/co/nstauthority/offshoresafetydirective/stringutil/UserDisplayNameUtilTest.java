package uk.co.nstauthority.offshoresafetydirective.stringutil;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.userutil.UserDisplayNameUtil;

class UserDisplayNameUtilTest {

  @Test
  void getUserDisplayName_whenTitle_thenTitleIncludedInDisplayName() {
    var title = "Dr";
    var forename = "Forename";
    var surname = "Surname";
    assertThat(UserDisplayNameUtil.getUserDisplayName(title, forename, surname)).isEqualTo("Dr Forename Surname");
  }

  @Test
  void getUserDisplayName_whenNoTitle_thenTitleNotIncludedInDisplayName() {
    var forename = "Forename";
    var surname = "Surname";
    assertThat(UserDisplayNameUtil.getUserDisplayName(null, forename, surname)).isEqualTo("Forename Surname");
  }

  @Test
  void getUserDisplayName_whenNoForename_thenForenameNotIncludedInDisplayName() {
    var title = "Dr";
    var surname = "Surname";
    assertThat(UserDisplayNameUtil.getUserDisplayName(title, null, surname)).isEqualTo("Dr Surname");
  }

  @Test
  void getUserDisplayName_whenNoSurname_thenSurnameNotIncludedInDisplayName() {
    var title = "Dr";
    var forename = "Forename";
    assertThat(UserDisplayNameUtil.getUserDisplayName(title, forename, null)).isEqualTo("Dr Forename");
  }

}