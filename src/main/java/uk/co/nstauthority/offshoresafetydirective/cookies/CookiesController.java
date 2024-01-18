package uk.co.nstauthority.offshoresafetydirective.cookies;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;

@Controller
@RequestMapping("/cookies")
@Unauthenticated
public class CookiesController {

  public static final String PAGE_NAME = "Cookies";

  @GetMapping
  public ModelAndView getCookiePreferences() {
    return new ModelAndView("osd/cookies/cookies")
        .addObject("pageName", PAGE_NAME);
  }

}
