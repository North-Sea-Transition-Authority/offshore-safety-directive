package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EnergyPortalApiUtil {

  private static final String SERVICE_IDENTIFIER = "OSD";

  private EnergyPortalApiUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static String withRequestPurpose(String requestPurpose) {
    return "%s: %s".formatted(SERVICE_IDENTIFIER, requestPurpose);
  }

  public static String withLogCorrelationId(String logCorrelationId) {
    return "%s: %s".formatted(SERVICE_IDENTIFIER, logCorrelationId);
  }
}
