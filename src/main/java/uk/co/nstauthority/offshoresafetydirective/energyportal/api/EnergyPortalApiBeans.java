package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.energyportalapi.client.EnergyPortal;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceApi;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.client.subarea.SubareaApi;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.client.wellbore.WellboreApi;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalQueryCounter;

@Configuration
class EnergyPortalApiBeans {

  @Bean
  EnergyPortal energyPortal(
      EnergyPortalApiConfiguration energyPortalApiConfig,
      EnergyPortalQueryCounter energyPortalQueryCounter
  ) {
    return EnergyPortal.customConfiguration(
        energyPortalApiConfig.url(),
        energyPortalApiConfig.token(),
        EnergyPortal.DEFAULT_REQUEST_TIMEOUT_SECONDS,
        () -> new LogCorrelationId(CorrelationIdUtil.getCorrelationIdFromMdc()),
        energyPortalQueryCounter
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

  @Bean
  SubareaApi subareaApi(EnergyPortal energyPortal) {
    return new SubareaApi(energyPortal);
  }

  @Bean
  WellboreApi wellboreApi(EnergyPortal energyPortal) {
    return new WellboreApi(energyPortal);
  }

  @Bean
  LicenceApi licenceApi(EnergyPortal energyPortal) {
    return new LicenceApi(energyPortal);
  }
}
