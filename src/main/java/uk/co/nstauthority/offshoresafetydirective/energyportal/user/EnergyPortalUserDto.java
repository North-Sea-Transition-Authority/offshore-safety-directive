package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
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
) implements EmailRecipient {

  public String displayName() {
    return UserDisplayNameUtil.getUserDisplayName(title, forename, surname);
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }
}