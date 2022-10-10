package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.energyportalapi.client.EnergyPortal;
import uk.co.fivium.energyportalapi.client.user.UserApi;

@Configuration
class EnergyPortalApiBeans {

  @Bean
  EnergyPortal energyPortal(EnergyPortalApiConfiguration energyPortalApiConfiguration) {
    return EnergyPortal.defaultConfiguration(
        energyPortalApiConfiguration.url(),
        energyPortalApiConfiguration.token()
    );
  }

  @Bean
  UserApi userApi(EnergyPortal energyPortal) {
    return new UserApi(energyPortal);
  }
}
