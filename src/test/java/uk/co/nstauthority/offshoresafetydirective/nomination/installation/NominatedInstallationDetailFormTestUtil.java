package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominatedInstallationDetailFormTestUtil {

  private NominatedInstallationDetailFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static class NominatedInstallationDetailFormBuilder {

    private List<Integer> installations = List.of(1, 2);
    private Boolean forAllInstallationPhases = true;
    private Boolean developmentDesignPhase;
    private Boolean developmentConstructionPhase;
    private Boolean developmentInstallationPhase;
    private Boolean developmentCommissioningPhase;
    private Boolean developmentProductionPhase;
    private Boolean decommissioningPhase;

    public NominatedInstallationDetailFormBuilder withInstallations(List<Integer> installations) {
      this.installations = installations;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withForAllInstallationPhases(Boolean forAllInstallationPhases) {
      this.forAllInstallationPhases = forAllInstallationPhases;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentDesignPhase(Boolean developmentDesignPhase) {
      this.developmentDesignPhase = developmentDesignPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentConstructionPhase(Boolean developmentConstructionPhase) {
      this.developmentConstructionPhase = developmentConstructionPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentInstallationPhase(Boolean developmentInstallationPhase) {
      this.developmentInstallationPhase = developmentInstallationPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentCommissioningPhase(Boolean developmentCommissioningPhase) {
      this.developmentCommissioningPhase = developmentCommissioningPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentProductionPhase(Boolean developmentProductionPhase) {
      this.developmentProductionPhase = developmentProductionPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    public NominatedInstallationDetailForm build() {
      return new NominatedInstallationDetailForm()
          .setInstallations(installations)
          .setForAllInstallationPhases(forAllInstallationPhases)
          .setDevelopmentDesignPhase(developmentDesignPhase)
          .setDevelopmentConstructionPhase(developmentConstructionPhase)
          .setDevelopmentInstallationPhase(developmentInstallationPhase)
          .setDevelopmentCommissioningPhase(developmentCommissioningPhase)
          .setDevelopmentProductionPhase(developmentProductionPhase)
          .setDecommissioningPhase(decommissioningPhase);
    }
  }
}
