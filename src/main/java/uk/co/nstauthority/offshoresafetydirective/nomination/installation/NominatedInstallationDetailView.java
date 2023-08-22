package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;

public class NominatedInstallationDetailView {

  private final List<InstallationDto> installations;
  private final Boolean forAllInstallationPhases;
  private final List<InstallationPhase> installationPhases;
  private final List<LicenceDto> licences;

  public NominatedInstallationDetailView() {
    this(Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList());
  }

  public NominatedInstallationDetailView(List<InstallationDto> installations, Boolean forAllInstallationPhases,
                                         List<InstallationPhase> installationPhases, List<LicenceDto> licences) {
    this.installations = installations;
    this.forAllInstallationPhases = forAllInstallationPhases;
    this.installationPhases = installationPhases;
    this.licences = licences;
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

  public List<LicenceDto> getLicences() {
    return licences;
  }
}
