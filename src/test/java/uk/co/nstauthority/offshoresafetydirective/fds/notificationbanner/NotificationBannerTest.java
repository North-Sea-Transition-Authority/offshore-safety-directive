package uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class NotificationBannerTest {

  @ParameterizedTest
  @EnumSource(NotificationBannerType.class)
  void build_whenCustomTitleProvided_thenCustomTitleReturned(NotificationBannerType notificationBannerType) {

    var notificationBanner = NotificationBanner.builder()
        .withCustomTitle("my custom title")
        .withBannerType(notificationBannerType)
        .build();

    assertThat(notificationBanner.getTitle()).isEqualTo("my custom title");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void build_whenNoCustomTitleAndSuccessType_thenDefaultSuccessTitleReturned(String titleInput) {

    var notificationBanner = NotificationBanner.builder()
        .withCustomTitle(titleInput)
        .withBannerType(NotificationBannerType.SUCCESS)
        .build();

    assertThat(notificationBanner.getTitle()).isEqualTo("Success");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void build_whenNoCustomTitleAndInfoType_thenDefaultInfoTitleReturned(String titleInput) {

    var notificationBanner = NotificationBanner.builder()
        .withCustomTitle(titleInput)
        .withBannerType(NotificationBannerType.INFO)
        .build();

    assertThat(notificationBanner.getTitle()).isEqualTo("Information");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void build_whenCustomTitleBlankAndNoTypeProvided_thenEmptyString(String titleInput) {

    var notificationBanner = NotificationBanner.builder()
        .withCustomTitle(titleInput)
        .withBannerType(null)
        .build();

    assertThat(notificationBanner.getTitle()).isEmpty();
  }

  @Test
  void build_whenNoBannerType_thenDefaultIsInfo() {

    var notificationBanner = NotificationBanner.builder().build();

    assertThat(notificationBanner.getType()).isEqualTo(NotificationBannerType.INFO);
  }
}