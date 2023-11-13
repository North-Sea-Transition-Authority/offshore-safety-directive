package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;

@Component
public class EnergyPortalApiWrapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnergyPortalApiWrapper.class);

  public <T> T makeRequest(RequestPurpose requestPurpose, Function<LogCorrelationId, T> request) {
    var logCorrelationId = new LogCorrelationId(CorrelationIdUtil.getCorrelationIdFromMdc());
    logEpaRequest(logCorrelationId, requestPurpose);
    return request.apply(logCorrelationId);
  }

  private void logEpaRequest(LogCorrelationId logCorrelationId, RequestPurpose requestPurpose) {
    LOGGER.info("Making request to EPA with correlation id %s and request purpose (%s)"
        .formatted(logCorrelationId.id(), requestPurpose.purpose()));
  }
}
