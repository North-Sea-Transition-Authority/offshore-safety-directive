package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

public class AppointmentTimelineItemDto {

  private final boolean canManageAppointments;
  private final boolean isMemberOfRegulatorTeam;
  private final boolean canViewNominations;
  private final boolean canViewConsultations;
  private final boolean isTerminated;

  private AppointmentTimelineItemDto(boolean canManageAppointments,
                                     boolean isMemberOfRegulatorTeam,
                                     boolean canViewNominations,
                                     boolean canViewConsultations,
                                     boolean isTerminated) {

    this.canManageAppointments = canManageAppointments;
    this.isMemberOfRegulatorTeam = isMemberOfRegulatorTeam;
    this.canViewNominations = canViewNominations;
    this.canViewConsultations = canViewConsultations;
    this.isTerminated = isTerminated;
  }

  public boolean canManageAppointments() {
    return canManageAppointments;
  }

  public boolean isMemberOfRegulatorTeam() {
    return isMemberOfRegulatorTeam;
  }

  public boolean canViewNominations() {
    return canViewNominations;
  }

  public boolean canViewConsultations() {
    return canViewConsultations;
  }

  public boolean isTerminated() {
    return isTerminated;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private boolean canManageAppointments;
    private boolean isMemberOfRegulatorTeam;
    private boolean canViewNominations;
    private boolean canViewConsultations;
    private boolean isTerminated;

    private Builder() {
    }

    public Builder withCanManageAppointments(boolean canManageAppointments) {
      this.canManageAppointments = canManageAppointments;
      return this;
    }

    public Builder withMemberOfRegulatorTeam(boolean memberOfRegulatorTeam) {
      isMemberOfRegulatorTeam = memberOfRegulatorTeam;
      return this;
    }

    public Builder withCanViewNominations(boolean canViewNominations) {
      this.canViewNominations = canViewNominations;
      return this;
    }

    public Builder withCanViewConsultations(boolean canViewConsultations) {
      this.canViewConsultations = canViewConsultations;
      return this;
    }

    public Builder isTerminated(boolean isTerminated) {
      this.isTerminated = isTerminated;
      return this;
    }

    public AppointmentTimelineItemDto build() {
      return new AppointmentTimelineItemDto(
          canManageAppointments,
          isMemberOfRegulatorTeam,
          canViewNominations,
          canViewConsultations,
          isTerminated
      );
    }
  }

}
