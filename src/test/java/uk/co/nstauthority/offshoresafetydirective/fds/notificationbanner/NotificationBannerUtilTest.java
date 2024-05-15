package uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class NotificationBannerUtilTest {

  @Test
  void applyNotificationBanner() {
    var redirectAttributes = new RedirectAttributesModelMap();
    var notificationBanner = NotificationBanner.builder().build();
    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

    assertThat(redirectAttributes.getFlashAttributes().get("flash")).isEqualTo(notificationBanner);
  }
}