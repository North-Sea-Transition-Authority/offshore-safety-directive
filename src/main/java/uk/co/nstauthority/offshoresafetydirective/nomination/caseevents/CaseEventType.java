package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

public enum CaseEventType {

  QA_CHECKS("QA checks completed", TeamType.REGULATOR),
  NO_OBJECTION_DECISION("No objections"),
  OBJECTION_DECISION("Objected"),
  WITHDRAWN("Withdrawn"),
  CONFIRM_APPOINTMENT("Confirm appointment"),
  GENERAL_NOTE("General note", TeamType.REGULATOR),
  NOMINATION_SUBMITTED("Nomination submitted"),
  SENT_FOR_CONSULTATION("Sent for consultation", TeamType.REGULATOR),
  CONSULTATION_RESPONSE("Consultation response", TeamType.REGULATOR),
  UPDATE_REQUESTED("Update requested")
  ;

  private final String screenDisplayText;
  private final EnumSet<TeamType> visibleForTeamTypes;

  CaseEventType(String screenDisplayText, TeamType... visibleForTeamTypes) {
    this.screenDisplayText = screenDisplayText;
    this.visibleForTeamTypes = EnumSet.copyOf(List.of(visibleForTeamTypes));
  }

  CaseEventType(String screenDisplayText) {
    this.screenDisplayText = screenDisplayText;
    visibleForTeamTypes = EnumSet.copyOf(Arrays.stream(TeamType.values()).toList());
  }

  public String getScreenDisplayText() {
    return screenDisplayText;
  }

  public static boolean isValidForTeamType(Collection<TeamType> teamTypes, CaseEventType caseEventType) {
    return teamTypes.stream().anyMatch(caseEventType.visibleForTeamTypes::contains);
  }
}
