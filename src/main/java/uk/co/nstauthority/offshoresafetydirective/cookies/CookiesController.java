package uk.co.nstauthority.offshoresafetydirective.cookies;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsProperties;

@Controller
@RequestMapping("/cookies")
@Unauthenticated
public class CookiesController {

  public static final String PAGE_NAME = "Cookies";
  private final AnalyticsProperties analyticsProperties;

  @Autowired
  public CookiesController(AnalyticsProperties analyticsProperties) {
    this.analyticsProperties = analyticsProperties;
  }

  @GetMapping
  public ModelAndView getCookiePreferences() {
    var serviceAnalyticIdentifier = stripGoogleCharactersFromIdentifier(
        analyticsProperties.serviceAnalyticIdentifier()
    );

    var energyPortalAnalyticIdentifier = stripGoogleCharactersFromIdentifier(
        analyticsProperties.energyPortalAnalyticIdentifier()
    );


    return new ModelAndView("osd/cookies/cookies")
        .addObject("pageName", PAGE_NAME)
        .addObject("serviceAnalyticIdentifier", serviceAnalyticIdentifier)
        .addObject("energyPortalAnalyticIdentifier", energyPortalAnalyticIdentifier);
  }

  private String stripGoogleCharactersFromIdentifier(String identifier) {
    if (StringUtils.isBlank(identifier)) {
      throw new IllegalStateException("No analytic identifier provided");
    }
    return StringUtils.stripStart(identifier, "G-");
  }

}
