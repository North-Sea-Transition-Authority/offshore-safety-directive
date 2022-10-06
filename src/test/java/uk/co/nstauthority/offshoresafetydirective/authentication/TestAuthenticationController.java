package uk.co.nstauthority.offshoresafetydirective.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
@ActiveProfiles("test")
class TestAuthenticationController {

  private final UserDetailService userDetailService;

  @Autowired
  TestAuthenticationController(UserDetailService userDetailService) {
    this.userDetailService = userDetailService;
  }

  @GetMapping("/secured")
  ModelAndView requiresUserEndpoint() {
    return new ModelAndView()
        .addObject("user", userDetailService.getUserDetail());
  }

}

