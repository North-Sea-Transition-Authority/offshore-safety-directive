package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class NominatedInstallationDetailTestUtil {

  private NominatedInstallationDetailTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static NominatedInstallationDetailBuilder builder() {
    return new NominatedInstallationDetailBuilder();
  }
  
  public static class NominatedInstallationDetailBuilder {

    private Integer id = 851;
    private NominationDetail nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    private Boolean forAllInstallationPhases = true;
    private Boolean developmentDesignPhase;
    private Boolean developmentConstructionPhase;
    private Boolean developmentInstallationPhase;
    private Boolean developmentCommissioningPhase;
    private Boolean developmentProductionPhase;
    private Boolean decommissioningPhase;

    public NominatedInstallationDetailBuilder withId(Integer id) {
      this.id = id;
      return this;
    }

    public NominatedInstallationDetailBuilder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public NominatedInstallationDetailBuilder withForAllInstallationPhases(Boolean forAllInstallationPhases) {
      this.forAllInstallationPhases = forAllInstallationPhases;
      return this;
    }

    public NominatedInstallationDetailBuilder withDevelopmentDesignPhase(Boolean developmentDesignPhase) {
      this.developmentDesignPhase = developmentDesignPhase;
      return this;
    }

    public NominatedInstallationDetailBuilder withDevelopmentConstructionPhase(Boolean developmentConstructionPhase) {
      this.developmentConstructionPhase = developmentConstructionPhase;
      return this;
    }

    public NominatedInstallationDetailBuilder withDevelopmentInstallationPhase(Boolean developmentInstallationPhase) {
      this.developmentInstallationPhase = developmentInstallationPhase;
      return this;
    }

    public NominatedInstallationDetailBuilder withDevelopmentCommissioningPhase(Boolean developmentCommissioningPhase) {
      this.developmentCommissioningPhase = developmentCommissioningPhase;
      return this;
    }

    public NominatedInstallationDetailBuilder withDevelopmentProductionPhase(Boolean developmentProductionPhase) {
      this.developmentProductionPhase = developmentProductionPhase;
      return this;
    }

    public NominatedInstallationDetailBuilder withDecommissioningPhase(Boolean decommissioningPhase) {
      this.decommissioningPhase = decommissioningPhase;
      return this;
    }

    public NominatedInstallationDetail build() {
      var nominatedInstallationDetail = new NominatedInstallationDetail(id);
      nominatedInstallationDetail.setNominationDetail(nominationDetail);
      nominatedInstallationDetail.setForAllInstallationPhases(forAllInstallationPhases);
      nominatedInstallationDetail.setDevelopmentDesignPhase(developmentDesignPhase);
      nominatedInstallationDetail.setDevelopmentConstructionPhase(developmentConstructionPhase);
      nominatedInstallationDetail.setDevelopmentInstallationPhase(developmentInstallationPhase);
      nominatedInstallationDetail.setDevelopmentCommissioningPhase(developmentCommissioningPhase);
      nominatedInstallationDetail.setDevelopmentProductionPhase(developmentProductionPhase);
      nominatedInstallationDetail.setDecommissioningPhase(decommissioningPhase);
      return nominatedInstallationDetail;
    }
  }
}
