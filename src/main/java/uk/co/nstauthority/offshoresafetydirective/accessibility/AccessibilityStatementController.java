package uk.co.nstauthority.offshoresafetydirective.accessibility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;

@Controller
@RequestMapping("/accessibility-statement")
@Unauthenticated
public class AccessibilityStatementController {

  public static final String PAGE_NAME = "Accessibility statement";

  private final TechnicalSupportConfigurationProperties technicalSupportConfiguration;
  private final AccessibilityStatementConfigurationProperties accessibilityStatementConfiguration;

  @Autowired
  public AccessibilityStatementController(TechnicalSupportConfigurationProperties technicalSupportConfiguration,
                                          AccessibilityStatementConfigurationProperties accessibilityStatementConfiguration) {
    this.technicalSupportConfiguration = technicalSupportConfiguration;
    this.accessibilityStatementConfiguration = accessibilityStatementConfiguration;
  }

  @GetMapping
  public ModelAndView getAccessibilityStatement() {
    return new ModelAndView("osd/accessibility/accessibilityStatement")
        .addObject("technicalSupport", technicalSupportConfiguration)
        .addObject("accessibilityConfig", accessibilityStatementConfiguration);
  }
}
