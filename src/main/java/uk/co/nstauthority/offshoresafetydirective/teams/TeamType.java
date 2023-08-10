package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Arrays;
import java.util.Optional;

public enum TeamType {

  REGULATOR("Licensing authority", 10, "licensing-authority"),
  CONSULTEE("Consultee", 20, "consultee");

  private final String displayText;
  private final int displayOrder;
  private final String urlSlug;

  TeamType(String displayText, int displayOrder, String urlSlug) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.urlSlug = urlSlug;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getUrlSlug() {
    return urlSlug;
  }

  public static Optional<TeamType> getTeamTypeFromUrlSlug(String slug) {
    return Arrays.stream(values())
        .filter(teamType -> teamType.urlSlug.equals(slug))
        .findFirst();
  }

}