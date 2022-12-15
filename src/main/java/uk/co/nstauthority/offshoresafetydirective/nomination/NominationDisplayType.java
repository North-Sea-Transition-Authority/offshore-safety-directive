package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

public enum NominationDisplayType {

  WELL("Wells"),
  INSTALLATION("Installations"),
  BOTH("Wells and installations"),
  NOT_PROVIDED("Not provided");

  private final String displayText;

  NominationDisplayType(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static NominationDisplayType getByWellSelectionTypeAndHasInstallations(WellSelectionType wellSelectionType,
                                                                                NominationHasInstallations hasInstallations) {

    var wellSelection = wellSelectionType == null ? WellSelectionType.NO_WELLS : wellSelectionType;
    var expectingWells = !wellSelection.equals(WellSelectionType.NO_WELLS);

    var installationSelection = hasInstallations == null ? NominationHasInstallations.NO : hasInstallations;
    var expectingInstallations = installationSelection.equals(NominationHasInstallations.YES);

    if (expectingWells && expectingInstallations) {
      return NominationDisplayType.BOTH;
    } else if (expectingWells) {
      return NominationDisplayType.WELL;
    } else if (expectingInstallations) {
      return NominationDisplayType.INSTALLATION;
    }
    return NominationDisplayType.NOT_PROVIDED;
  }
}
