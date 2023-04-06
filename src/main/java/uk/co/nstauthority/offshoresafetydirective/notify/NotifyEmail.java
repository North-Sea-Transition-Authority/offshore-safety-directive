package uk.co.nstauthority.offshoresafetydirective.notify;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;

public class NotifyEmail {

  private final NotifyTemplate template;
  private final Map<String, String> personalisations;

  private NotifyEmail(NotifyTemplate template, Map<String, String> personalisations) {
    this.template = template;
    this.personalisations = personalisations;
  }

  public NotifyTemplate getTemplate() {
    return template;
  }

  public Map<String, String> getPersonalisations() {
    return ImmutableMap.copyOf(personalisations);
  }

  public void sendAsTestEmail(boolean isTestEmail) {
    this.personalisations.put("IS_TEST_EMAIL", isTestEmail ? "yes" : "no");

    if (isTestEmail) {
      personalisations.put("SUBJECT_PREFIX", "**TEST EMAIL** ");
    }
  }

  public static Builder builder(NotifyTemplate template,
                                ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties) {
    return new Builder(template)
        // Set TEST_EMAIL to "no" by default
        .addPersonalisation("IS_TEST_EMAIL", "no")
        .addPersonalisation("SUBJECT_PREFIX", "")
        .addPersonalisation(
            "SERVICE_NAME",
            serviceBrandingConfigurationProperties.getServiceConfigurationProperties().name()
        );
  }

  public static class Builder {

    private final NotifyTemplate template;
    private final Map<String, String> personalisations = new HashMap<>();

    private Builder(NotifyTemplate template) {
      this.template = template;
    }

    public Builder addPersonalisation(String personalisationKey, String value) {
      personalisations.put(personalisationKey, value);
      return this;
    }

    public NotifyEmail build() {
      return new NotifyEmail(template, personalisations);
    }
  }
}
