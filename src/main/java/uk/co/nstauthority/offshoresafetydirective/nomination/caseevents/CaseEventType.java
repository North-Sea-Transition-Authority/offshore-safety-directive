package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

public enum CaseEventType {

  QA_CHECKS("QA checks completed", List.of(TeamType.REGULATOR)),
  NO_OBJECTION_DECISION("No objections"),
  OBJECTION_DECISION("Objected"),
  WITHDRAWN("Withdrawn"),
  CONFIRM_APPOINTMENT("Confirm appointment"),
  GENERAL_NOTE("General note", List.of(TeamType.REGULATOR)),
  NOMINATION_SUBMITTED("Nomination submitted"),
  SENT_FOR_CONSULTATION("Sent for consultation", List.of(TeamType.REGULATOR)),
  CONSULTATION_RESPONSE("Consultation response", List.of(TeamType.REGULATOR)),
  UPDATE_REQUESTED("Update requested"),
  ;

  private final String screenDisplayText;
  private final List<TeamType> teamTypesWithVisibiltiy;

  CaseEventType(String screenDisplayText, List<TeamType> teamTypesWithVisbibiltiy) {
    this.screenDisplayText = screenDisplayText;
    this.teamTypesWithVisibiltiy = teamTypesWithVisbibiltiy;
  }

  CaseEventType(String screenDisplayText) {
    this.screenDisplayText = screenDisplayText;
    teamTypesWithVisibiltiy = Arrays.stream(TeamType.values()).toList();
  }

  public String getScreenDisplayText() {
    return screenDisplayText;
  }

  
  public static boolean isValidForTeamType(Collection<TeamType> teamTypes, CaseEventType caseEventType) {
    return teamTypes.stream().anyMatch(caseEventType.teamTypesWithVisibiltiy::contains);
  }
}
