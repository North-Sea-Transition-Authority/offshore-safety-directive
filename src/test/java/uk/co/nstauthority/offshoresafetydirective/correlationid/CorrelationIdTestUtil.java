package uk.co.nstauthority.offshoresafetydirective.correlationid;

public class CorrelationIdTestUtil {

  public static void setCorrelationIdOnMdc(String value) {
    CorrelationIdUtil.setCorrelationIdOnMdc(value);
  }
}
