package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominatedInstallationDetailViewTestUtil {

  private NominatedInstallationDetailViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private List<InstallationDto> installations = new ArrayList<>();
    private Boolean forAllInstallationPhases = true;
    private List<InstallationPhase> installationPhases = new ArrayList<>();

    public Builder withInstallations(List<InstallationDto> installations) {
      this.installations = installations;
      return this;
    }

    public Builder addInstallations(List<InstallationDto> installations) {
      this.installations.addAll(installations);
      return this;
    }

    public Builder addInstallation(InstallationDto installationDto) {
      this.installations.add(installationDto);
      return this;
    }

    public Builder withForAllInstallationPhases(Boolean forAllInstallationPhases) {
      this.forAllInstallationPhases = forAllInstallationPhases;
      return this;
    }

    public Builder withInstallationPhases(List<InstallationPhase> installationPhases) {
      this.installationPhases = installationPhases;
      return this;
    }

    public Builder addInstallationPhases(List<InstallationPhase> installationPhases) {
      this.installationPhases.addAll(installationPhases);
      return this;
    }

    public Builder addInstallationPhase(InstallationPhase installationPhase) {
      this.installationPhases.add(installationPhase);
      return this;
    }

    private Builder() {
    }

    public NominatedInstallationDetailView build() {
      return new NominatedInstallationDetailView(installations, forAllInstallationPhases, installationPhases);
    }
  }
}