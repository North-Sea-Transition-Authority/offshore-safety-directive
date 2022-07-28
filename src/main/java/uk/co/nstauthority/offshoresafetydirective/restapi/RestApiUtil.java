package uk.co.nstauthority.offshoresafetydirective.restapi;

import org.apache.commons.lang3.StringUtils;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

public class RestApiUtil {

  private static final String SEARCH_TERM_PARAM_NAME = "term";

  private RestApiUtil() {
    throw new IllegalStateException("RestApiUtil is a util class and should not be instantiated");
  }

  public static String route(Object methodCall) {
    return StringUtils.removeEnd(ReverseRouter.route(methodCall), String.format("?%s", SEARCH_TERM_PARAM_NAME));
  }
}
