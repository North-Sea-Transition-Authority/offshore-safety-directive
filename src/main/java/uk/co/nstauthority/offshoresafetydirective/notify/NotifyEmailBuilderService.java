package uk.co.nstauthority.offshoresafetydirective.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;

@Service
public class NotifyEmailBuilderService {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  @Autowired
  public NotifyEmailBuilderService(ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties) {
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
  }

  public NotifyEmail.Builder builder(NotifyTemplate notifyTemplate) {
    return NotifyEmail.builder(notifyTemplate, serviceBrandingConfigurationProperties);
  }
}
