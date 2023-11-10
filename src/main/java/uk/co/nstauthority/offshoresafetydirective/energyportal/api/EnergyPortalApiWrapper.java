package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;

@Component
public class EnergyPortalApiWrapper {

  private final ServiceConfigurationProperties serviceConfigurationProperties;

  private static final Logger LOGGER = LoggerFactory.getLogger(EnergyPortalApiWrapper.class);

  @Autowired
  public EnergyPortalApiWrapper(ServiceConfigurationProperties serviceConfigurationProperties) {
    this.serviceConfigurationProperties = serviceConfigurationProperties;
  }

  public <T> T makeRequest(BiFunction<LogCorrelationId, RequestPurpose, T> request) {
    var logCorrelationId = new LogCorrelationId(CorrelationIdUtil.getCorrelationIdFromMdc());
    var requestPurpose = getRequestPurpose();
    logEpaRequest(logCorrelationId, requestPurpose);
    return request.apply(logCorrelationId, requestPurpose);
  }

  public <T> T makeRequest(RequestPurpose requestPurpose, Function<LogCorrelationId, T> request) {
    var logCorrelationId = new LogCorrelationId(CorrelationIdUtil.getCorrelationIdFromMdc());
    logEpaRequest(logCorrelationId, requestPurpose);
    return request.apply(logCorrelationId);
  }

  private RequestPurpose getRequestPurpose() {

    var callingMethod = StackWalker.getInstance()
        .walk(frames -> frames
            .skip(2) // the first frame is this method, second is the BiFunction request so skip to get the real caller
            .findFirst()
            .map(stackFrame -> "%s.%s".formatted(stackFrame.getClassName(), stackFrame.getMethodName()))
        )
        .orElseThrow(() -> new RuntimeException("Failed to find a stack frame for request purpose"));

    return new RequestPurpose("%s: %s".formatted(getServiceIdentifier(), callingMethod));
  }

  private void logEpaRequest(LogCorrelationId logCorrelationId, RequestPurpose requestPurpose) {
    LOGGER.info("Making request to EPA with correlation id %s and request purpose (%s)"
        .formatted(logCorrelationId.id(), requestPurpose.purpose()));
  }

  private String getServiceIdentifier() {
    return serviceConfigurationProperties.mnemonic();
  }

}
