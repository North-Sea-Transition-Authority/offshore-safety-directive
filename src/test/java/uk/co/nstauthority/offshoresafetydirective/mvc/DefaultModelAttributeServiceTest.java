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
import uk.co.nstauthority.offshoresafetydirective.accessibility.AccessibilityStatementController;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.branding.WonsContactConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.WonsContactConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.contact.ContactInformationController;
import uk.co.nstauthority.offshoresafetydirective.cookies.CookiesController;
import uk.co.nstauthority.offshoresafetydirective.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.offshoresafetydirective.feedback.FeedbackController;
import uk.co.nstauthority.offshoresafetydirective.footer.FooterItem;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.MapEntryAssert;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

class DefaultModelAttributeServiceTest {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties =
      ServiceBrandingConfigurationPropertiesTestUtil
          .builder()
          .build();

  private final WonsContactConfigurationProperties wonsContactConfigurationProperties =
      WonsContactConfigurationPropertiesTestUtil
          .builder()
          .build();

  private final AnalyticsProperties analyticsProperties = AnalyticsPropertiesTestUtil.builder().build();

  private UserDetailService userDetailService;

  private TopNavigationService topNavigationService;

  private DefaultModelAttributeService defaultModelAttributeService;

  @BeforeEach
  void setup() {
    userDetailService = mock(UserDetailService.class);
    topNavigationService = mock(TopNavigationService.class);
    defaultModelAttributeService = new DefaultModelAttributeService(
        serviceBrandingConfigurationProperties,
        wonsContactConfigurationProperties,
        userDetailService,
        topNavigationService,
        analyticsProperties);
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
                "navigationItems", List.of(topNavigationItem),
                "footerItems", List.of(
                    new FooterItem("Accessibility statement",
                        ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())),
                    new FooterItem("Contact us",
                        ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage())),
                    new FooterItem("Cookies",
                        ReverseRouter.route(on(CookiesController.class).getCookiePreferences())),
                    new FooterItem("Feedback",
                        ReverseRouter.route(on(FeedbackController.class).getFeedback(null)))
                ),
                "feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)),
                "wonsEmail", wonsContactConfigurationProperties.email(),
                "cookiesStatementUrl", ReverseRouter.route(on(CookiesController.class).getCookiePreferences()),
                "analytics", analyticsProperties
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
                "navigationItems", List.of(topNavigationItem),
                "footerItems", List.of(
                    new FooterItem("Accessibility statement",
                        ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())),
                    new FooterItem("Contact us",
                        ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage())),
                    new FooterItem("Cookies",
                        ReverseRouter.route(on(CookiesController.class).getCookiePreferences()))
                ),
                "feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)),
                "wonsEmail", wonsContactConfigurationProperties.email(),
                "cookiesStatementUrl", ReverseRouter.route(on(CookiesController.class).getCookiePreferences()),
                "analytics", analyticsProperties
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

    MapEntryAssert.thenAssertThat(attributes)
        .hasKeyWithValue("serviceBranding", serviceBrandingConfigurationProperties.getServiceConfigurationProperties())
        .hasKeyWithValue("customerBranding", serviceBrandingConfigurationProperties.getCustomerConfigurationProperties())
        .hasKeyWithValue("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
        .hasKeyWithValue("loggedInUser", user)
        .hasKeyWithValue("navigationItems", List.of(topNavigationItem))
        .hasKeyWithValue("currentEndPoint", "/request-uri")
        .hasKeyWithValue("footerItems", List.of(
            new FooterItem("Accessibility statement",
                ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())),
            new FooterItem("Contact us",
                ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage())),
            new FooterItem("Cookies",
                ReverseRouter.route(on(CookiesController.class).getCookiePreferences())),
            new FooterItem("Feedback",
                ReverseRouter.route(on(FeedbackController.class).getFeedback(null)))))
        .hasKeyWithValue("feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)))
        .hasKeyWithValue("wonsEmail", wonsContactConfigurationProperties.email())
        .hasKeyWithValue("cookiesStatementUrl", ReverseRouter.route(on(CookiesController.class).getCookiePreferences()))
        .hasKeyWithValue("analytics", analyticsProperties);
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
                "currentEndPoint", "/request-uri",
                "footerItems", List.of(
                    new FooterItem("Accessibility statement",
                        ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())),
                    new FooterItem("Contact us",
                        ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage())),
                    new FooterItem("Cookies",
                        ReverseRouter.route(on(CookiesController.class).getCookiePreferences()))
                ),
                "feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)),
                "wonsEmail", wonsContactConfigurationProperties.email(),
                "cookiesStatementUrl", ReverseRouter.route(on(CookiesController.class).getCookiePreferences()),
                "analytics", analyticsProperties
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
                "navigationItems", List.of(topNavigationItem),
                "footerItems", List.of(
                    new FooterItem("Accessibility statement",
                        ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())),
                    new FooterItem("Contact us",
                        ReverseRouter.route(on(ContactInformationController.class).getContactInformationPage())),
                    new FooterItem("Cookies",
                        ReverseRouter.route(on(CookiesController.class).getCookiePreferences())),
                    new FooterItem("Feedback",
                        ReverseRouter.route(on(FeedbackController.class).getFeedback(null)))
                ),
                "feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)),
                "wonsEmail", wonsContactConfigurationProperties.email(),
                "cookiesStatementUrl", ReverseRouter.route(on(CookiesController.class).getCookiePreferences()),
                "analytics", analyticsProperties
            )
        );
  }
}