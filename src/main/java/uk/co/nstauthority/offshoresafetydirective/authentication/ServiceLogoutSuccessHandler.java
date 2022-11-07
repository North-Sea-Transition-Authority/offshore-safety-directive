package uk.co.nstauthority.offshoresafetydirective.authentication;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;

@Component
public class ServiceLogoutSuccessHandler implements LogoutSuccessHandler {

  private final EnergyPortalConfiguration energyPortalConfiguration;

  @Autowired
  public ServiceLogoutSuccessHandler(EnergyPortalConfiguration energyPortalConfiguration) {
    this.energyPortalConfiguration = energyPortalConfiguration;
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request,
                              HttpServletResponse response,
                              Authentication authentication) throws IOException {
    response.sendRedirect(energyPortalConfiguration.logoutUrl());
  }
}
