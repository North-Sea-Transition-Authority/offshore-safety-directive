package uk.co.nstauthority.offshoresafetydirective.energyportal;

import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.starter.LogCorrelationIdSupplier;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;

@Component
class EnergyPortalAccountsLogCorrelationIdSupplier implements LogCorrelationIdSupplier {

  @Override
  public String get() {
    return CorrelationIdUtil.getCorrelationIdFromMdc();
  }
}
