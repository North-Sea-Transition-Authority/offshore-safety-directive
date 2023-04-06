package uk.co.nstauthority.offshoresafetydirective.notify;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;

class NotifyEmailBuilderServiceTest {

  private ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private NotifyEmailBuilderService notifyEmailBuilderService;

  @BeforeEach
  void setUp() {
    this.serviceBrandingConfigurationProperties = NotifyEmailTestUtil.serviceBrandingConfigurationProperties;
    this.notifyEmailBuilderService = new NotifyEmailBuilderService(serviceBrandingConfigurationProperties);
  }

  @Test
  void verifyBuilder() {
    var notifyTemplate = NotifyTemplate.EMAIL_DELIVERY_FAILED;
    var result = notifyEmailBuilderService.builder(notifyTemplate).build();
    assertThat(result.getTemplate()).isEqualTo(notifyTemplate);

    assertThat(result.getPersonalisations())
        .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "IS_TEST_EMAIL", "no",
                "SERVICE_NAME", serviceBrandingConfigurationProperties.getServiceConfigurationProperties().name(),
                "SUBJECT_PREFIX", ""
            )
        );
  }
}