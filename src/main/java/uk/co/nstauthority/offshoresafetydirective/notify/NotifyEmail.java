package uk.co.nstauthority.offshoresafetydirective.notify;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;

public class NotifyEmail {

  public static final String RECIPIENT_NAME_PERSONALISATION_KEY = "RECIPIENT_IDENTIFIER";

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

    var serviceBranding = serviceBrandingConfigurationProperties.getServiceConfigurationProperties();
    var customerBranding = serviceBrandingConfigurationProperties.getCustomerConfigurationProperties();

    return new Builder(template)
        // Set TEST_EMAIL to "no" by default
        .addPersonalisation("IS_TEST_EMAIL", "no")
        .addPersonalisation("SUBJECT_PREFIX", "")
        .addPersonalisation("SERVICE_FULL_NAME", serviceBranding.name())
        .addPersonalisation("SERVICE_MNEMONIC", serviceBranding.mnemonic())
        .addPersonalisation("REGULATION_NAME_SHORT", serviceBranding.regulationNameShort())
        .addPersonalisation("REGULATION_NAME_LONG", serviceBranding.regulationNameLong())
        .addPersonalisation("REGULATOR_BUSINESS_EMAIL_ADDRESS", customerBranding.businessEmailAddress())
        .addPersonalisation("SALUTATION", "Dear")
        .addPersonalisation("VALEDICTION", "Kind regards")
        .addPersonalisation(RECIPIENT_NAME_PERSONALISATION_KEY, "%s user".formatted(serviceBranding.mnemonic()));
  }

  public static class Builder {

    private final NotifyTemplate template;
    private final Map<String, String> personalisations = new HashMap<>();

    private Builder(NotifyTemplate template) {
      this.template = template;
    }

    public Builder addPersonalisations(Map<String, String> personalisations) {
      this.personalisations.putAll(personalisations);
      return this;
    }

    public Builder addPersonalisation(String personalisationKey, String value) {
      personalisations.put(personalisationKey, value);
      return this;
    }

    public Builder addRecipientIdentifier(String recipientIdentifier) {
      personalisations.put(RECIPIENT_NAME_PERSONALISATION_KEY, recipientIdentifier);
      return this;
    }

    public NotifyEmail build() {
      return new NotifyEmail(template, new HashMap<>(personalisations));
    }
  }
}