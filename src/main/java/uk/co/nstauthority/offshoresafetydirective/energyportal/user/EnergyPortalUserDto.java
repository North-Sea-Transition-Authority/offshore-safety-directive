package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import uk.co.nstauthority.offshoresafetydirective.userutil.UserDisplayNameUtil;

public record EnergyPortalUserDto(
    long webUserAccountId,
    String title,
    String forename,
    String surname,
    String emailAddress,
    String telephoneNumber,
    boolean isSharedAccount,
    boolean canLogin
) {

  public String displayName() {
    return UserDisplayNameUtil.getUserDisplayName(title, forename, surname);
  }
}