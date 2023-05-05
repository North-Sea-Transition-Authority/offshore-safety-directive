package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Service
public class DefaultModelAttributeService {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  private final UserDetailService userDetailService;

  private final TopNavigationService topNavigationService;

  @Autowired
  public DefaultModelAttributeService(ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
                                      UserDetailService userDetailService,
                                      TopNavigationService topNavigationService) {
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
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

    getUser().ifPresent(serviceUserDetail -> attributes.put("loggedInUser", serviceUserDetail));

    if (httpServletRequest != null) {
      attributes.put("currentEndPoint", httpServletRequest.getRequestURI());
    }
  }

  private Optional<ServiceUserDetail> getUser() {
    try {
      return Optional.of(userDetailService.getUserDetail());
    } catch (InvalidAuthenticationException exception) {
      return Optional.empty();
    }
  }
}
