package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ControllerAdvice
public class DefaultPageControllerAdvice {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private final TopNavigationService topNavigationService;
  private final UserDetailService userDetailService;

  @Autowired
  DefaultPageControllerAdvice(ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
                              TopNavigationService topNavigationService,
                              UserDetailService userDetailService) {
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.topNavigationService = topNavigationService;
    this.userDetailService = userDetailService;
  }

  @ModelAttribute
  void addDefaultModelAttributes(Model model, HttpServletRequest request) {
    addBrandingAttributes(model);
    addCommonUrls(model);
    addTopNavigationItems(model, request);
    addUser(model);
  }

  @InitBinder
  void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  private void addBrandingAttributes(Model model) {
    model.addAttribute(
        "serviceBranding",
        serviceBrandingConfigurationProperties.getServiceConfigurationProperties()
    );
    model.addAttribute(
        "customerBranding",
        serviceBrandingConfigurationProperties.getCustomerConfigurationProperties()
    );
  }

  private void addTopNavigationItems(Model model, HttpServletRequest request) {
    model.addAttribute("navigationItems", topNavigationService.getTopNavigationItems());
    model.addAttribute("currentEndPoint", request.getRequestURI());
  }

  private void addCommonUrls(Model model) {
    model.addAttribute("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
  }

  private void addUser(Model model) {
    try {
      var user = userDetailService.getUserDetail();
      model.addAttribute("loggedInUser", user);
    } catch (InvalidAuthenticationException exception) {
     // catch exception as unauthenticated endpoints won't have a logged-in user
    }
  }
}
