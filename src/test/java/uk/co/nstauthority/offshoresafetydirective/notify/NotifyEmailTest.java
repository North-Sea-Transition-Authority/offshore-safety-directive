package uk.co.nstauthority.offshoresafetydirective.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class NotifyEmailTest {

  @Test
  void verifyDefaults() {
    var notifyEmail = NotifyEmail.builder(
        NotifyTemplate.EMAIL_DELIVERY_FAILED,
        NotifyEmailTestUtil.serviceBrandingConfigurationProperties
    ).build();

    assertThat(notifyEmail)
        .extracting(NotifyEmail::getPersonalisations)
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "IS_TEST_EMAIL", "no",
                "SERVICE_NAME", NotifyEmailTestUtil.serviceBrandingConfigurationProperties
                    .getServiceConfigurationProperties().name(),
                "SUBJECT_PREFIX", ""
            ));
  }


}