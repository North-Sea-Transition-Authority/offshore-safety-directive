package uk.co.nstauthority.offshoresafetydirective.configuration;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.mvc.error.DefaultClientErrorController;

class ServiceAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request,
                     HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException, ServletException {
    var unauthorisedErrorUrl = "%s%s".formatted(
        request.getServletContext().getContextPath(),
        ReverseRouter.route(on(DefaultClientErrorController.class).getUnauthorisedErrorPage())
    );
    response.sendRedirect(unauthorisedErrorUrl);
  }
}
