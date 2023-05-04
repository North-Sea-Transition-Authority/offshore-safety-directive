package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationResponseForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PearsPortalReferenceForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.WonsPortalReferenceForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationRequestUpdateForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationForm;

public class CaseProcessingFormDto {


  private final NominationQaChecksForm nominationQaChecksForm;
  private final NominationDecisionForm nominationDecisionForm;
  private final WithdrawNominationForm withdrawNominationForm;
  private final ConfirmNominationAppointmentForm confirmNominationAppointmentForm;
  private final GeneralCaseNoteForm generalCaseNoteForm;
  private final PearsPortalReferenceForm pearsPortalReferenceForm;
  private final WonsPortalReferenceForm wonsPortalReferenceForm;
  private final NominationConsultationResponseForm nominationConsultationResponseForm;
  private final NominationRequestUpdateForm nominationRequestUpdateForm;

  private CaseProcessingFormDto(NominationQaChecksForm nominationQaChecksForm,
                                NominationDecisionForm nominationDecisionForm,
                                WithdrawNominationForm withdrawNominationForm,
                                ConfirmNominationAppointmentForm confirmNominationAppointmentForm,
                                GeneralCaseNoteForm generalCaseNoteForm,
                                PearsPortalReferenceForm pearsPortalReferenceForm,
                                WonsPortalReferenceForm wonsPortalReferenceForm,
                                NominationConsultationResponseForm nominationConsultationResponseForm,
                                NominationRequestUpdateForm nominationRequestUpdateForm) {

    this.nominationQaChecksForm = nominationQaChecksForm;
    this.nominationDecisionForm = nominationDecisionForm;
    this.withdrawNominationForm = withdrawNominationForm;
    this.confirmNominationAppointmentForm = confirmNominationAppointmentForm;
    this.generalCaseNoteForm = generalCaseNoteForm;
    this.pearsPortalReferenceForm = pearsPortalReferenceForm;
    this.wonsPortalReferenceForm = wonsPortalReferenceForm;
    this.nominationConsultationResponseForm = nominationConsultationResponseForm;
    this.nominationRequestUpdateForm = nominationRequestUpdateForm;
  }

  public NominationQaChecksForm getNominationQaChecksForm() {
    return nominationQaChecksForm;
  }

  public NominationDecisionForm getNominationDecisionForm() {
    return nominationDecisionForm;
  }

  public WithdrawNominationForm getWithdrawNominationForm() {
    return withdrawNominationForm;
  }

  public ConfirmNominationAppointmentForm getConfirmNominationAppointmentForm() {
    return confirmNominationAppointmentForm;
  }

  public GeneralCaseNoteForm getGeneralCaseNoteForm() {
    return generalCaseNoteForm;
  }

  public PearsPortalReferenceForm getPearsPortalReferenceForm() {
    return pearsPortalReferenceForm;
  }

  public WonsPortalReferenceForm getWonsPortalReferenceForm() {
    return wonsPortalReferenceForm;
  }

  public NominationConsultationResponseForm getNominationConsultationResponseForm() {
    return nominationConsultationResponseForm;
  }

  public NominationRequestUpdateForm getNominationRequestUpdateForm() {
    return nominationRequestUpdateForm;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private NominationQaChecksForm nominationQaChecksForm = new NominationQaChecksForm();
    private NominationDecisionForm nominationDecisionForm = new NominationDecisionForm();
    private WithdrawNominationForm withdrawNominationForm = new WithdrawNominationForm();
    private ConfirmNominationAppointmentForm confirmNominationAppointmentForm = new ConfirmNominationAppointmentForm();
    private GeneralCaseNoteForm generalCaseNoteForm = new GeneralCaseNoteForm();
    private PearsPortalReferenceForm pearsPortalReferenceForm = new PearsPortalReferenceForm();
    private WonsPortalReferenceForm wonsPortalReferenceForm = new WonsPortalReferenceForm();
    private NominationConsultationResponseForm nominationConsultationResponseForm = new NominationConsultationResponseForm();
    private NominationRequestUpdateForm nominationRequestUpdateForm = new NominationRequestUpdateForm();

    private Builder() {
    }

    public Builder withNominationQaChecksForm(NominationQaChecksForm nominationQaChecksForm) {
      this.nominationQaChecksForm = nominationQaChecksForm;
      return this;
    }

    public Builder withNominationDecisionForm(NominationDecisionForm nominationDecisionForm) {
      this.nominationDecisionForm = nominationDecisionForm;
      return this;
    }

    public Builder withWithdrawNominationForm(WithdrawNominationForm withdrawNominationForm) {
      this.withdrawNominationForm = withdrawNominationForm;
      return this;
    }

    public Builder withConfirmNominationAppointmentForm(
        ConfirmNominationAppointmentForm confirmNominationAppointmentForm) {
      this.confirmNominationAppointmentForm = confirmNominationAppointmentForm;
      return this;
    }

    public Builder withGeneralCaseNoteForm(GeneralCaseNoteForm generalCaseNoteForm) {
      this.generalCaseNoteForm = generalCaseNoteForm;
      return this;
    }

    public Builder withPearsPortalReferenceForm(PearsPortalReferenceForm pearsPortalReferenceForm) {
      this.pearsPortalReferenceForm = pearsPortalReferenceForm;
      return this;
    }

    public Builder withWonsPortalReferenceForm(WonsPortalReferenceForm wonsPortalReferenceForm) {
      this.wonsPortalReferenceForm = wonsPortalReferenceForm;
      return this;
    }

    public Builder withNominationConsultationResponseForm(
        NominationConsultationResponseForm nominationConsultationResponseForm
    ) {
      this.nominationConsultationResponseForm = nominationConsultationResponseForm;
      return this;
    }

    public Builder withNominationRequestUpdateForm(NominationRequestUpdateForm nominationRequestUpdateForm) {
      this.nominationRequestUpdateForm = nominationRequestUpdateForm;
      return this;
    }

    public CaseProcessingFormDto build() {
      return new CaseProcessingFormDto(nominationQaChecksForm, nominationDecisionForm, withdrawNominationForm,
          confirmNominationAppointmentForm, generalCaseNoteForm, pearsPortalReferenceForm, wonsPortalReferenceForm,
          nominationConsultationResponseForm, nominationRequestUpdateForm);
    }
  }

}
