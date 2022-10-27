package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

public record EnergyPortalUserDto(
    int webUserAccountId,
    String title,
    String forename,
    String surname,
    String emailAddress,
    String telephoneNumber
) {

  public String displayName() {
    return "%s %s".formatted(forename, surname);
  }
}