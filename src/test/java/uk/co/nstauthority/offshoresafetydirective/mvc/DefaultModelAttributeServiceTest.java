package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

class DefaultModelAttributeServiceTest {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties =
      ServiceBrandingConfigurationPropertiesTestUtil
          .builder()
          .build();

  private UserDetailService userDetailService;

  private TopNavigationService topNavigationService;

  private DefaultModelAttributeService defaultModelAttributeService;

  @BeforeEach
  void setup() {
    userDetailService = mock(UserDetailService.class);
    topNavigationService = mock(TopNavigationService.class);
    defaultModelAttributeService = new DefaultModelAttributeService(
        serviceBrandingConfigurationProperties,
        userDetailService,
        topNavigationService
    );
  }

  @Test
  void addDefaultModelAttributes_withoutRequest_verifyDefaultAttributes() {

    var user = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(user);

    var topNavigationItem = new TopNavigationItem("name", "/url");

    given(topNavigationService.getTopNavigationItems())
        .willReturn(List.of(topNavigationItem));

    var attributes = new HashMap<String, Object>();

    defaultModelAttributeService.addDefaultModelAttributes(attributes);

    assertThat(attributes)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties(),
                "customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties(),
                "serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                "loggedInUser", user,
                "navigationItems", List.of(topNavigationItem)
            )
        );
  }

  @Test
  void addDefaultModelAttributes_withoutRequestAndNoUser_verifyDefaultAttributes() {

    given(userDetailService.getUserDetail())
        .willThrow(new InvalidAuthenticationException("exception"));

    var topNavigationItem = new TopNavigationItem("name", "/url");

    given(topNavigationService.getTopNavigationItems())
        .willReturn(List.of(topNavigationItem));

    var attributes = new HashMap<String, Object>();

    defaultModelAttributeService.addDefaultModelAttributes(attributes);

    assertThat(attributes)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties(),
                "customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties(),
                "serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                "navigationItems", List.of(topNavigationItem)
            )
        );
  }

  @Test
  void addDefaultModelAttributes_withRequest_verifyDefaultAttributes() {

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/request-uri");

    var user = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(user);

    var topNavigationItem = new TopNavigationItem("name", "/url");

    given(topNavigationService.getTopNavigationItems())
        .willReturn(List.of(topNavigationItem));

    var attributes = new HashMap<String, Object>();

    defaultModelAttributeService.addDefaultModelAttributes(attributes, request);

    assertThat(attributes)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties(),
                "customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties(),
                "serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                "loggedInUser", user,
                "navigationItems", List.of(topNavigationItem),
                "currentEndPoint", "/request-uri"
            )
        );
  }

  @Test
  void addDefaultModelAttributes_withRequestAndNoUser_verifyDefaultAttributes() {

    given(userDetailService.getUserDetail())
        .willThrow(new InvalidAuthenticationException("exception"));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/request-uri");

    var topNavigationItem = new TopNavigationItem("name", "/url");

    given(topNavigationService.getTopNavigationItems())
        .willReturn(List.of(topNavigationItem));

    var attributes = new HashMap<String, Object>();

    defaultModelAttributeService.addDefaultModelAttributes(attributes, request);

    assertThat(attributes)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties(),
                "customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties(),
                "serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                "navigationItems", List.of(topNavigationItem),
                "currentEndPoint", "/request-uri"
            )
        );
  }

  @Test
  void addDefaultModelAttributes_withNullRequest_verifyDefaultAttributes() {

    MockHttpServletRequest request = null;

    var user = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail())
        .willReturn(user);

    var topNavigationItem = new TopNavigationItem("name", "/url");

    given(topNavigationService.getTopNavigationItems())
        .willReturn(List.of(topNavigationItem));

    var attributes = new HashMap<String, Object>();

    defaultModelAttributeService.addDefaultModelAttributes(attributes, request);

    assertThat(attributes)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties(),
                "customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties(),
                "serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                "loggedInUser", user,
                "navigationItems", List.of(topNavigationItem)
            )
        );
  }
}