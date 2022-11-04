package uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner;

// TODO OSDOP-290 - Enforce that title and heading fields are provided
public class NotificationBanner {

  private final String title;
  private final String heading;
  private final String content;
  private final NotificationBannerType type;

  private NotificationBanner(String title, String heading, String content, NotificationBannerType type) {
    this.title = title;
    this.heading = heading;
    this.content = content;
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public String getHeading() {
    return heading;
  }

  public String getContent() {
    return content;
  }

  public NotificationBannerType getType() {
    return type;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String title = "";
    private String heading = "";
    private String content = "";
    private NotificationBannerType notificationBannerType = NotificationBannerType.INFO;

    private Builder() {
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withHeading(String heading) {
      this.heading = heading;
      return this;
    }

    public Builder withContent(String content) {
      this.content = content;
      return this;
    }

    public Builder withBannerType(NotificationBannerType notificationBannerType) {
      this.notificationBannerType = notificationBannerType;
      return this;
    }

    public NotificationBanner build() {
      return new NotificationBanner(title, heading, content, notificationBannerType);
    }
  }
}
