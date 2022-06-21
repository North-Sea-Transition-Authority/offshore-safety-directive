package uk.co.nstauthority.offshoresafetydirective.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;

@ControllerAdvice
class DefaultPageControllerAdvice {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  @Autowired
  DefaultPageControllerAdvice(ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties) {
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
  }

  @ModelAttribute
  void addDefaultModelAttributes(Model model) {
    model.addAttribute(
        "serviceBranding",
        serviceBrandingConfigurationProperties.getServiceConfigurationProperties()
    );
    model.addAttribute(
        "customerBranding",
        serviceBrandingConfigurationProperties.getCustomerConfigurationProperties()
    );
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }
}
