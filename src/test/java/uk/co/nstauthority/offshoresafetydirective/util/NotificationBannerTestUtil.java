package uk.co.nstauthority.offshoresafetydirective.util;

import static org.springframework.test.util.AssertionErrors.assertEquals;

import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;

public class NotificationBannerTestUtil {

  private NotificationBannerTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ResultMatcher notificationBanner(NotificationBanner notificationBanner) {
    return result -> {
      var actualBanner = (NotificationBanner) result.getFlashMap().get("flash");
      assertEquals(
          "Failed comparing notification banner title [%s], [%s]"
              .formatted(actualBanner.getTitle(), notificationBanner.getTitle()),
          notificationBanner.getTitle(),
          actualBanner.getTitle()
      );
      assertEquals(
          "Failed comparing notification banner heading [%s], [%s]"
              .formatted(actualBanner.getHeading(), notificationBanner.getHeading()),
          notificationBanner.getHeading(),
          actualBanner.getHeading()
      );
      assertEquals(
          "Failed comparing notification banner content [%s], [%s]"
              .formatted(actualBanner.getContent(), notificationBanner.getContent()),
          notificationBanner.getContent(),
          actualBanner.getContent()
      );
      assertEquals(
          "Failed comparing notification banner type [%s], [%s]"
              .formatted(actualBanner.getType(), notificationBanner.getType()),
          notificationBanner.getType(),
          actualBanner.getType()
      );
    };
  }

}
