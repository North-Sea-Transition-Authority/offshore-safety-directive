package uk.co.nstauthority.offshoresafetydirective.authenticationTest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

  @GetMapping("/secured")
  public ModelAndView renderSecured() {
    return new ModelAndView("osd/auth/auth");
  }

}
