package uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NotificationBannerUtil {

  private NotificationBannerUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static void applyNotificationBanner(RedirectAttributes redirectAttributes,
                                             NotificationBanner notificationBanner) {

    redirectAttributes.addFlashAttribute("flash", notificationBanner);
  }

}
