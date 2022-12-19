package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.energyportalapi.client.EnergyPortal;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
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

  @Bean
  FieldApi fieldApi(EnergyPortal energyPortal) {
    return new FieldApi(energyPortal);
  }

  @Bean
  FacilityApi facilityApi(EnergyPortal energyPortal) {
    return new FacilityApi(energyPortal);
  }

  @Bean
  OrganisationApi organisationApi(EnergyPortal energyPortal) {
    return new OrganisationApi(energyPortal);
  }

}
