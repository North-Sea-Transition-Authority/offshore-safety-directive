package uk.co.nstauthority.offshoresafetydirective.configuration;

import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@ActiveProfiles({"test", "development"})
class TestController {

  private static final String TEST_VIEW_NAME = "view-name";

  @GetMapping("/security-test")
  ModelAndView testEndpoint() {
    return new ModelAndView(TEST_VIEW_NAME);
  }

  @GetMapping("/security/test")
  ModelAndView testEndpointWithMultiUrlPath() {
    return new ModelAndView(TEST_VIEW_NAME);
  }

}