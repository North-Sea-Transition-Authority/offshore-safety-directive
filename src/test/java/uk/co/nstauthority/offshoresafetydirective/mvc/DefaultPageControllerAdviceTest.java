package uk.co.nstauthority.offshoresafetydirective.mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;

@ContextConfiguration(classes = {
    DefaultPageControllerAdviceTest.TestController.class,
    DefaultPageControllerAdvice.class
})
class DefaultPageControllerAdviceTest extends AbstractControllerTest {

  @MockBean
  private DefaultModelAttributeService defaultModelAttributeService;

  @Test
  void addDefaultModelAttributes_verifyInteractions() throws Exception {

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    mockMvc.perform(
        get(ReverseRouter.route(on(TestController.class).testEndpoint()))
            .with(user(loggedInUser))
    );

    then(defaultModelAttributeService)
        .should(onlyOnce())
        .addDefaultModelAttributes(anyMap(), any(HttpServletRequest.class));
  }

  // Dummy application to stop the @WebMvcTest loading more than it needs
  @SpringBootApplication
  static class TestApplication {
  }

  @Controller
  @RequestMapping("/endpoint")
  static class TestController {

    private static final String VIEW_NAME = "test-template";

    @GetMapping()
    ModelAndView testEndpoint() {
      return new ModelAndView(VIEW_NAME);
    }
  }

}