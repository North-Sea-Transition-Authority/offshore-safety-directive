package uk.co.nstauthority.offshoresafetydirective.contact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;

@Controller
@RequestMapping("/contact")
@Unauthenticated
public class ContactInformationController {

  public static final String PAGE_NAME = "Contact us";
  private final TechnicalSupportConfigurationProperties technicalSupportConfiguration;
  private final CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  public ContactInformationController(TechnicalSupportConfigurationProperties technicalSupportConfiguration,
                                      CustomerConfigurationProperties customerConfigurationProperties) {
    this.technicalSupportConfiguration = technicalSupportConfiguration;
    this.customerConfigurationProperties = customerConfigurationProperties;
  }

  @GetMapping
  public ModelAndView getContactInformationPage() {
    return new ModelAndView("osd/contact/contactInformation")
        .addObject("pageName", PAGE_NAME)
        .addObject("businessSupport", customerConfigurationProperties)
        .addObject("technicalSupport", technicalSupportConfiguration);
  }
}
