package uk.co.nstauthority.offshoresafetydirective.energyportal.api;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdTestUtil;

class EnergyPortalApiWrapperTest {

  private static final ServiceConfigurationProperties serviceConfigurationProperties
      = ServiceConfigurationPropertiesTestUtil.builder().build();

  private final EnergyPortalApiWrapper energyPortalApiWrapper = new EnergyPortalApiWrapper(
      serviceConfigurationProperties
  );

  @Test
  void makeRequest_verifyLogCorrelationId() {
    var correlationId = UUID.randomUUID().toString();

    CorrelationIdTestUtil.setCorrelationIdOnMdc(correlationId);

    var returnedCorrelationId = energyPortalApiWrapper.makeRequest(
        new RequestPurpose("a request purpose"),
        this::returnLogCorrelationId
    );

    assertThat(returnedCorrelationId).isEqualTo(correlationId);
  }

  private String returnLogCorrelationId(LogCorrelationId logCorrelationId) {
    return logCorrelationId.id();
  }
}