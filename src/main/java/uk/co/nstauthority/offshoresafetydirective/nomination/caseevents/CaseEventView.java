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
  private final Instant eventInstant;
  private final String formattedEventTime;
  private final String customDatePrompt;
  private final List<CaseEventFileView> fileViews;
  private final String customFilePrompt;

  private CaseEventView(int nominationVersion, String customVersionPrompt, String title, String body,
                        String customBodyPrompt,
                        String createdBy, String customCreatorPrompt, Instant createdInstant,
                        Instant eventInstant, String formattedEventTime,
                        String customDatePrompt, List<CaseEventFileView> fileViews, String customFilePrompt) {
    this.nominationVersion = nominationVersion;
    this.customVersionPrompt = customVersionPrompt;
    this.title = title;
    this.body = body;
    this.customBodyPrompt = customBodyPrompt;
    this.createdBy = createdBy;
    this.customCreatorPrompt = customCreatorPrompt;
    this.createdInstant = createdInstant;
    this.eventInstant = eventInstant;
    this.formattedEventTime = formattedEventTime;
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

  public Instant getEventInstant() {
    return eventInstant;
  }

  public String getFormattedEventTime() {
    return formattedEventTime;
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

  public static Builder builder(String title, int nominationVersion, Instant createdInstant, Instant eventInstant,
                                String createdBy) {
    return new Builder(title, nominationVersion, createdInstant, eventInstant, createdBy);
  }

  public static class Builder {

    private final String title;
    private final int nominationVersion;
    private String customVersionPrompt;
    private String body;
    private String customBodyPrompt;
    private final String createdBy;
    private String customCreatorPrompt;
    private Instant eventInstant;
    private final Instant createdInstant;
    private String formattedEventTime;
    private String customDatePrompt;
    private List<CaseEventFileView> fileViews;
    private String customFilePrompt;

    private Builder(String title, int nominationVersion, Instant createdInstant, Instant eventInstant,
                    String createdBy) {
      this.title = title;
      this.nominationVersion = nominationVersion;
      this.createdInstant = createdInstant;
      this.eventInstant = eventInstant;
      this.formattedEventTime = DateUtil.formatDateTime(eventInstant);
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

    public Builder withEventInstant(Instant eventInstant, String formattedEventTime) {
      this.eventInstant = eventInstant;
      this.formattedEventTime = formattedEventTime;
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
          nominationVersion, customVersionPrompt, title, body, customBodyPrompt, createdBy, customCreatorPrompt,
          createdInstant, eventInstant, formattedEventTime, customDatePrompt, fileViews, customFilePrompt);
    }
  }

}
