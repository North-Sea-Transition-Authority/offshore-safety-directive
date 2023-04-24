package uk.co.nstauthority.offshoresafetydirective.notify;

import java.util.HashMap;
import java.util.Map;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NotifyEmailTestUtil {

  private NotifyEmailTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties =
      new ServiceBrandingConfigurationProperties(
          new CustomerConfigurationProperties("name", "mnem", "/", "test@wios.co.uk"),
          ServiceConfigurationPropertiesTestUtil.builder().build()
      );

  static Map<String, String> getDefaultEmailProperties(
      ServiceBrandingConfigurationProperties serviceBrandingProperties
  ) {

    ServiceConfigurationProperties serviceBranding = serviceBrandingProperties.getServiceConfigurationProperties();
    CustomerConfigurationProperties customerBranding = serviceBrandingProperties.getCustomerConfigurationProperties();

    Map<String, String> personalisations = new HashMap<>();

    personalisations.put("IS_TEST_EMAIL", "no");
    personalisations.put("SUBJECT_PREFIX", "");
    personalisations.put("SERVICE_FULL_NAME", serviceBranding.name());
    personalisations.put("SERVICE_MNEMONIC", serviceBranding.mnemonic());
    personalisations.put("REGULATION_NAME_SHORT", serviceBranding.regulationNameShort());
    personalisations.put("REGULATION_NAME_LONG", serviceBranding.regulationNameLong());
    personalisations.put("REGULATOR_BUSINESS_EMAIL_ADDRESS", customerBranding.businessEmailAddress());
    personalisations.put("SALUTATION", "Dear");
    personalisations.put("VALEDICTION", "Kind regards");
    personalisations.put(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, "%s user".formatted(serviceBranding.mnemonic()));

    return personalisations;
  }

}
