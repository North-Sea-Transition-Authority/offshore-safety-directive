package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public record InstallationAddToListView(InstallationDto installation) implements AddToListItem {

  @Override
  public String getId() {
    return String.valueOf(installation.id());
  }

  @Override
  public String getName() {
    return installation.name();
  }

  @Override
  public boolean isValid() {
    return NominatedInstallationDetailFormValidator.isValidInstallation(installation);
  }
}
