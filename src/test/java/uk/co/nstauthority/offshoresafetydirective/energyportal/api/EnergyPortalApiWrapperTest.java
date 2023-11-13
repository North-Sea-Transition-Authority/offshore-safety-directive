package uk.co.nstauthority.offshoresafetydirective.energyportal.api;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;

class EnergyPortalApiWrapperTest {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");

  private final EnergyPortalApiWrapper energyPortalApiWrapper = new EnergyPortalApiWrapper();

  @Test
  void makeRequest_verifyLogCorrelationId() {
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var returnedCorrelationId = energyPortalApiWrapper.makeRequest(
        REQUEST_PURPOSE,
        this::returnLogCorrelationId
    );

    assertThat(returnedCorrelationId).isEqualTo(correlationId);
  }

  private String returnLogCorrelationId(LogCorrelationId logCorrelationId) {
    return logCorrelationId.id();
  }
}