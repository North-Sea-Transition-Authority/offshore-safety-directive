package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;

public class NominatedInstallationDetailView {

  private final List<InstallationDto> installations;
  private final Boolean forAllInstallationPhases;
  private final List<InstallationPhase> installationPhases;

  public NominatedInstallationDetailView() {
    this(Collections.emptyList(), null, Collections.emptyList());
  }

  public NominatedInstallationDetailView(List<InstallationDto> installations, Boolean forAllInstallationPhases,
                                         List<InstallationPhase> installationPhases) {
    this.installations = installations;
    this.forAllInstallationPhases = forAllInstallationPhases;
    this.installationPhases = installationPhases;
  }

  public List<InstallationDto> getInstallations() {
    return installations;
  }

  public Boolean getForAllInstallationPhases() {
    return forAllInstallationPhases;
  }

  public List<InstallationPhase> getInstallationPhases() {
    return installationPhases;
  }
}
