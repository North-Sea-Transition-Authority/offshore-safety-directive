package uk.co.nstauthority.offshoresafetydirective.energyportal.api;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;

class EnergyPortalApiWrapperTest {

  private final ServiceConfigurationProperties serviceConfigurationProperties = new ServiceConfigurationProperties(
      "name",
      "mnemonic"
  );

  private final EnergyPortalApiWrapper energyPortalApiWrapper = new EnergyPortalApiWrapper(
      serviceConfigurationProperties
  );

  @Test
  void makeRequest_verifyLogCorrelationId() {
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var returnedCorrelationId = energyPortalApiWrapper.makeRequest(this::returnLogCorrelationId);

    assertThat(returnedCorrelationId).isEqualTo(correlationId);
  }

  @Test
  void makeRequest_verifyRequestPurpose() {

    var requestPurpose = energyPortalApiWrapper.makeRequest(this::returnRequestPurpose);

    assertThat(requestPurpose).isEqualTo("%s: %s.%s".formatted(
        serviceConfigurationProperties.mnemonic(),
        this.getClass().getName(),
        "makeRequest_verifyRequestPurpose"
    ));
  }

  private String returnLogCorrelationId(LogCorrelationId logCorrelationId, RequestPurpose requestPurpose) {
    return logCorrelationId.id();
  }

  private String returnRequestPurpose(LogCorrelationId logCorrelationId, RequestPurpose requestPurpose) {
    return requestPurpose.purpose();
  }
}