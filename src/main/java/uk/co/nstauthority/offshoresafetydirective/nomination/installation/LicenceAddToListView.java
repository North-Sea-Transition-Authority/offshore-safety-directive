package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public record LicenceAddToListView(LicenceDto licence) implements AddToListItem {

  @Override
  public String getId() {
    return String.valueOf(licence.licenceId().id());
  }

  @Override
  public String getName() {
    return licence.licenceReference().value();
  }

  @Override
  public boolean isValid() {
    return true;
  }
}
