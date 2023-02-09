package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.Instant;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;

public class CaseEventView {

  private final int nominationVersion;
  private final String customVersionPrompt;
  private final String title;
  private final String body;
  private final String customBodyPrompt;
  private final String createdBy;
  private final String customCreatorPrompt;
  private final Instant createdInstant;
  private final String formattedCreatedTime;
  private final String customDatePrompt;
  private final List<CaseEventFileView> fileViews;
  private final String customFilePrompt;

  private CaseEventView(int nominationVersion, String customVersionPrompt, String title, String body, String customBodyPrompt,
                        String createdBy, String customCreatorPrompt, Instant createdInstant, String formattedCreatedTime,
                        String customDatePrompt, List<CaseEventFileView> fileViews, String customFilePrompt) {
    this.nominationVersion = nominationVersion;
    this.customVersionPrompt = customVersionPrompt;
    this.title = title;
    this.body = body;
    this.customBodyPrompt = customBodyPrompt;
    this.createdBy = createdBy;
    this.customCreatorPrompt = customCreatorPrompt;
    this.createdInstant = createdInstant;
    this.formattedCreatedTime = formattedCreatedTime;
    this.customDatePrompt = customDatePrompt;
    this.fileViews = fileViews;
    this.customFilePrompt = customFilePrompt;
  }

  public int getNominationVersion() {
    return nominationVersion;
  }

  public String getCustomVersionPrompt() {
    return customVersionPrompt;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public String getCustomBodyPrompt() {
    return customBodyPrompt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public String getCustomCreatorPrompt() {
    return customCreatorPrompt;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  public String getFormattedCreatedTime() {
    return formattedCreatedTime;
  }

  public String getCustomDatePrompt() {
    return customDatePrompt;
  }

  public List<CaseEventFileView> getFileViews() {
    return fileViews;
  }

  public String getCustomFilePrompt() {
    return customFilePrompt;
  }

  public static Builder builder(String title, int nominationVersion, Instant createdInstant, String createdBy) {
    return new Builder(title, nominationVersion, createdInstant, createdBy);
  }

  public static class Builder {

    private final String title;
    private final int nominationVersion;
    private String customVersionPrompt;
    private String body;
    private String customBodyPrompt;
    private final String createdBy;
    private String customCreatorPrompt;
    private Instant createdInstant;
    private String formattedCreatedTime;
    private String customDatePrompt;
    private List<CaseEventFileView> fileViews;
    private String customFilePrompt;

    private Builder(String title, int nominationVersion, Instant createdInstant, String createdBy) {
      this.title = title;
      this.nominationVersion = nominationVersion;
      this.createdInstant = createdInstant;
      this.formattedCreatedTime = DateUtil.formatDateTime(createdInstant);
      this.createdBy = createdBy;
    }

    public Builder withCustomVersionPrompt(String versionPrompt) {
      this.customVersionPrompt = versionPrompt;
      return this;
    }

    public Builder withBody(String body) {
      this.body = body;
      return this;
    }

    public Builder withCustomBodyPrompt(String bodyPrompt) {
      this.customBodyPrompt = bodyPrompt;
      return this;
    }

    public Builder withCustomCreatorPrompt(String creatorPrompt) {
      this.customCreatorPrompt = creatorPrompt;
      return this;
    }

    public Builder withCreatedInstant(Instant createdInstant, String formattedCreatedTime) {
      this.createdInstant = createdInstant;
      this.formattedCreatedTime = formattedCreatedTime;
      return this;
    }

    public Builder withCustomDatePrompt(String datePrompt) {
      this.customDatePrompt = datePrompt;
      return this;
    }

    public Builder withFileViews(List<CaseEventFileView> fileViews) {
      this.fileViews = fileViews;
      return this;
    }

    public Builder withCustomFilePrompt(String filePrompt) {
      this.customFilePrompt = filePrompt;
      return this;
    }

    public CaseEventView build() {
      return new CaseEventView(
          nominationVersion, customVersionPrompt, title, body, customBodyPrompt, createdBy, customCreatorPrompt, createdInstant,
          formattedCreatedTime, customDatePrompt, fileViews, customFilePrompt);
    }
  }

}
