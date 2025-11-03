package uk.co.nstauthority.offshoresafetydirective.audit;

import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;

public class AuditRevisionUtil {

  private static final ThreadLocal<ServiceUserDetail> fallbackAuditUser = new ThreadLocal<>();

  public static ServiceUserDetail getFallbackAuditUser() {
    return fallbackAuditUser.get();
  }

  public static void withFallbackAuditUser(ServiceUserDetail user, Runnable runnable) {
    fallbackAuditUser.set(user);
    try {
      runnable.run();
    } finally {
      fallbackAuditUser.remove();
    }
  }
}
