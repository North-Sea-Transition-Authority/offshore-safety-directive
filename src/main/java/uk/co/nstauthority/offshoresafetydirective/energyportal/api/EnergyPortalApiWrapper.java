package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;

@Component
public class EnergyPortalApiWrapper {

  private final ServiceConfigurationProperties serviceConfigurationProperties;

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
    LoggerUtil.info("%s (%s)".formatted(logCorrelationId.id(), requestPurpose.purpose()));
  }

  private String getServiceIdentifier() {
    return serviceConfigurationProperties.mnemonic();
  }

}
