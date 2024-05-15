package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Arrays;
import java.util.Objects;
import uk.co.fivium.energyportalapi.generated.types.MechanicalStatus;

public enum WellboreMechanicalStatus {

  PLANNED(
      MechanicalStatus.PLANNED,
      "Planned"
  ),
  DRILLING(
      MechanicalStatus.DRILLING,
      "Drilling"
  ),
  COMPLETED_OPERATING(
      MechanicalStatus.COMPLETED_OPERATING,
      "Completed (Operating)"
  ),
  COMPLETED_SHUT_IN(
      MechanicalStatus.COMPLETED_SHUT_IN,
      "Completed (Shut in)"
  ),
  PLUGGED(
      MechanicalStatus.PLUGGED,
      "Plugged"
  ),
  ABANDONMENT_PHASE_ONE(
      MechanicalStatus.ABANDONMENT_PHASE_ONE,
      "Abandoned Phase 1"
  ),
  ABANDONMENT_PHASE_TWO(
      MechanicalStatus.ABANDONMENT_PHASE_TWO,
      "Abandoned Phase 2"
  ),
  ABANDONMENT_PHASE_THREE(
      MechanicalStatus.ABANDONMENT_PHASE_THREE,
      "Abandoned Phase 3"
  );

  private final MechanicalStatus portalMechanicalStatus;

  private final String displayName;

  WellboreMechanicalStatus(MechanicalStatus portalMechanicalStatus, String displayName) {
    this.portalMechanicalStatus = portalMechanicalStatus;
    this.displayName = displayName;
  }

  MechanicalStatus getPortalMechanicalStatus() {
    return portalMechanicalStatus;
  }

  public String displayName() {
    return displayName;
  }

  static WellboreMechanicalStatus fromPortalMechanicalStatus(MechanicalStatus portalMechanicalStatus) {
    return Arrays.stream(WellboreMechanicalStatus.values())
        .filter(status -> Objects.equals(status.getPortalMechanicalStatus(), portalMechanicalStatus))
        .findFirst()
        .orElse(null);
  }
}
