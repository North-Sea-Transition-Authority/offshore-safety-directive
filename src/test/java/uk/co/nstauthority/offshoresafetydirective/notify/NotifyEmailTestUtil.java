package uk.co.nstauthority.offshoresafetydirective.notify;

import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NotifyEmailTestUtil {

  private NotifyEmailTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties =
      new ServiceBrandingConfigurationProperties(
          new CustomerConfigurationProperties("name", "mnem", "/", "test@wios.co.uk"),
          new ServiceConfigurationProperties("test", "tst")
      );

}
