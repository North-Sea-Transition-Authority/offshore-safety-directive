package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.accessibility.AccessibilityStatementController;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.WonsContactConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.contact.ContactInformationController;
import uk.co.nstauthority.offshoresafetydirective.feedback.FeedbackController;
import uk.co.nstauthority.offshoresafetydirective.footer.FooterItem;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Service
public class DefaultModelAttributeService {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  private final WonsContactConfigurationProperties wonsContactConfigurationProperties;

  private final UserDetailService userDetailService;

  private final TopNavigationService topNavigationService;

  @Autowired
  public DefaultModelAttributeService(ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
                                      WonsContactConfigurationProperties wonsContactConfigurationProperties,
                                      UserDetailService userDetailService,
                                      TopNavigationService topNavigationService) {
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.wonsContactConfigurationProperties = wonsContactConfigurationProperties;
    this.userDetailService = userDetailService;
    this.topNavigationService = topNavigationService;
  }

  public void addDefaultModelAttributes(Map<String, Object> attributes) {
    addDefaultModelAttributes(attributes, null);
  }

  public void addDefaultModelAttributes(Map<String, Object> attributes,
                                        @Nullable HttpServletRequest httpServletRequest) {

    attributes.put("serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties());
    attributes.put("customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties());
    attributes.put("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
    attributes.put("navigationItems", topNavigationService.getTopNavigationItems());
    attributes.put("footerItems", getFooterItems());
    attributes.put("feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)));
    attributes.put("wonsEmail", wonsContactConfigurationProperties.email());

    getUser().ifPresent(serviceUserDetail -> attributes.put("loggedInUser", serviceUserDetail));

    if (httpServletRequest != null) {
      attributes.put("currentEndPoint", httpServletRequest.getRequestURI());
    }
  }

  private List<FooterItem> getFooterItems() {
    var footerItems = new ArrayList<>(
        List.of(
            new FooterItem(AccessibilityStatementController.PAGE_NAME,
                ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())),
            new FooterItem(ContactInformationController.PAGE_NAME,
                ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage()))
        ));

    if (getUser().isPresent()) {
      footerItems.add(
          new FooterItem(FeedbackController.PAGE_NAME,
              ReverseRouter.route(on(FeedbackController.class).getFeedback(null))));
    }

    return footerItems;
  }

  private Optional<ServiceUserDetail> getUser() {
    try {
      return Optional.of(userDetailService.getUserDetail());
    } catch (InvalidAuthenticationException exception) {
      return Optional.empty();
    }
  }
}
