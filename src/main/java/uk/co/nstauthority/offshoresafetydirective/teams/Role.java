package uk.co.nstauthority.offshoresafetydirective.teams;

public enum Role {
  TEAM_MANAGER("Access manager", "Can add, remove and update members of this team"),
  THIRD_PARTY_TEAM_MANAGER(
      "Third party access manager",
      "Can manage organisation and consultee access to this service"
  ),
  NOMINATION_MANAGER("Manage nominations", "Can create, process and view nomination applications"),
  APPOINTMENT_MANAGER(
      "Manage well and installation appointments",
      "Can view and carry out updates to the system of record including corrections and terminations"
  ),
  VIEW_ANY_NOMINATION("Nomination viewer", "Can view any nomination"),
  CONSULTATION_MANAGER("Consultation coordinator", "Receives consultation requests and can view nomination forms"),
  CONSULTATION_PARTICIPANT("Consultee", "Can view nomination forms"),
  NOMINATION_SUBMITTER(
      "Nomination submitter",
      "Can create, edit, submit, view and manage nominations linked to this organisation"
  ),
  NOMINATION_EDITOR("Nomination editor", "Can edit and view nominations linked to this organisation"),
  NOMINATION_VIEWER("Nomination viewer", "Can view nominations");

  private final String name;

  private final String description;

  Role(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }
}
