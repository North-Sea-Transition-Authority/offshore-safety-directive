package uk.co.nstauthority.offshoresafetydirective.energyportal;

import org.springframework.stereotype.Component;
import uk.co.fivium.energyportalapi.client.QueryListener;
import uk.co.fivium.energyportalapi.client.RequestProperties;
import uk.co.nstauthority.offshoresafetydirective.metrics.QueryCounter;

@Component
public class EnergyPortalQueryCounter implements QueryListener {

  private final QueryCounter queryCounter;

  EnergyPortalQueryCounter(QueryCounter queryCounter) {
    this.queryCounter = queryCounter;
  }

  @Override
  public void onRequest(RequestProperties request) {
    queryCounter.incrementEpa();
  }
}
