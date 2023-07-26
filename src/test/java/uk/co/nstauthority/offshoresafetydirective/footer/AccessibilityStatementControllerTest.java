package uk.co.nstauthority.offshoresafetydirective.footer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.accessibility.AccessibilityStatementConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.accessibility.AccessibilityStatementController;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = {AccessibilityStatementController.class})
@EnableConfigurationProperties(value = {
    TechnicalSupportConfigurationProperties.class,
    AccessibilityStatementConfigurationProperties.class})
class AccessibilityStatementControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void renderAccessibilityStatement_assertModelProperties() throws Exception {
    var modelAndView = mockMvc.perform(
            get(
                ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement()))
                .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/accessibility/accessibilityStatement"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModel()).isNotNull();

    assertThat(modelAndView.getModel().get("accessibilityConfig")).isInstanceOf(AccessibilityStatementConfigurationProperties.class);
    assertThat(modelAndView.getModel().get("technicalSupport")).isInstanceOf(TechnicalSupportConfigurationProperties.class);

  }

  @SecurityTest
  void renderAccessibilityStatement_withNoUser_assertStatusOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement())))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/accessibility/accessibilityStatement"));
  }
}