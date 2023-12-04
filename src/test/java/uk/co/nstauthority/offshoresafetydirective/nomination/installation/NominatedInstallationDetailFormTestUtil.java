package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominatedInstallationDetailFormTestUtil {

  private NominatedInstallationDetailFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static NominatedInstallationDetailFormBuilder builder() {
    return new NominatedInstallationDetailFormBuilder();
  }

  public static class NominatedInstallationDetailFormBuilder {

    private List<String> installations = new ArrayList<>();
    private List<String> licences = new ArrayList<>();
    private Boolean forAllInstallationPhases = true;
    private Boolean developmentDesignPhase;
    private Boolean developmentConstructionPhase;
    private Boolean developmentInstallationPhase;
    private Boolean developmentCommissioningPhase;
    private Boolean developmentProductionPhase;
    private Boolean decommissioningPhase;

    public NominatedInstallationDetailFormBuilder withInstallations(List<String> installations) {
      this.installations = installations;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withInstallation(int installationId) {
      this.installations.add(String.valueOf(installationId));
      return this;
    }

    public NominatedInstallationDetailFormBuilder withLicences(List<String> licences) {
      this.licences = licences;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withLicence(Integer licenceId) {
      this.licences.add(String.valueOf(licenceId));
      return this;
    }

    public NominatedInstallationDetailFormBuilder withForAllInstallationPhases(Boolean forAllInstallationPhases) {
      this.forAllInstallationPhases = forAllInstallationPhases;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withForAllInstallationPhases(String forAllInstallationPhases) {
      this.forAllInstallationPhases = BooleanUtils.toBooleanObject(forAllInstallationPhases);
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentDesignPhase(Boolean developmentDesignPhase) {
      this.developmentDesignPhase = developmentDesignPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentDesignPhase(String developmentDesignPhase) {
      this.developmentDesignPhase = BooleanUtils.toBooleanObject(developmentDesignPhase);
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentConstructionPhase(Boolean developmentConstructionPhase) {
      this.developmentConstructionPhase = developmentConstructionPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentConstructionPhase(String developmentConstructionPhase) {
      this.developmentConstructionPhase = BooleanUtils.toBooleanObject(developmentConstructionPhase);
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentInstallationPhase(Boolean developmentInstallationPhase) {
      this.developmentInstallationPhase = developmentInstallationPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentInstallationPhase(String developmentInstallationPhase) {
      this.developmentInstallationPhase = BooleanUtils.toBooleanObject(developmentInstallationPhase);
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentCommissioningPhase(Boolean developmentCommissioningPhase) {
      this.developmentCommissioningPhase = developmentCommissioningPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentCommissioningPhase(String developmentCommissioningPhase) {
      this.developmentCommissioningPhase = BooleanUtils.toBooleanObject(developmentCommissioningPhase);
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentProductionPhase(Boolean developmentProductionPhase) {
      this.developmentProductionPhase = developmentProductionPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDevelopmentProductionPhase(String developmentProductionPhase) {
      this.developmentProductionPhase = BooleanUtils.toBooleanObject(developmentProductionPhase);
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    public NominatedInstallationDetailFormBuilder withDecommissioningPhase(String decommissioningPhase) {
      this.decommissioningPhase = BooleanUtils.toBooleanObject(decommissioningPhase);
      return this;
    }

    public NominatedInstallationDetailForm build() {

      var form = new NominatedInstallationDetailForm();
      form.setInstallations(installations);
      form.setForAllInstallationPhases(Objects.toString(forAllInstallationPhases, null));
      form.setLicences(licences);
      form.setDevelopmentDesignPhase(Objects.toString(developmentDesignPhase, null));
      form.setDevelopmentConstructionPhase(Objects.toString(developmentConstructionPhase, null));
      form.setDevelopmentInstallationPhase(Objects.toString(developmentInstallationPhase, null));
      form.setDevelopmentCommissioningPhase(Objects.toString(developmentCommissioningPhase, null));
      form.setDevelopmentProductionPhase(Objects.toString(developmentProductionPhase, null));
      form.setDecommissioningPhase(Objects.toString(decommissioningPhase, null));

      return form;
    }
  }
}
