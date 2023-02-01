package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationForm;

public class CaseProcessingFormDto {


  private final NominationQaChecksForm nominationQaChecksForm;
  private final NominationDecisionForm nominationDecisionForm;
  private final WithdrawNominationForm withdrawNominationForm;
  private final ConfirmNominationAppointmentForm confirmNominationAppointmentForm;
  private final GeneralCaseNoteForm generalCaseNoteForm;

  private CaseProcessingFormDto(NominationQaChecksForm nominationQaChecksForm,
                                NominationDecisionForm nominationDecisionForm,
                                WithdrawNominationForm withdrawNominationForm,
                                ConfirmNominationAppointmentForm confirmNominationAppointmentForm,
                                GeneralCaseNoteForm generalCaseNoteForm) {

    this.nominationQaChecksForm = nominationQaChecksForm;
    this.nominationDecisionForm = nominationDecisionForm;
    this.withdrawNominationForm = withdrawNominationForm;
    this.confirmNominationAppointmentForm = confirmNominationAppointmentForm;
    this.generalCaseNoteForm = generalCaseNoteForm;
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private NominationQaChecksForm nominationQaChecksForm = new NominationQaChecksForm();
    private NominationDecisionForm nominationDecisionForm = new NominationDecisionForm();
    private WithdrawNominationForm withdrawNominationForm = new WithdrawNominationForm();
    private ConfirmNominationAppointmentForm confirmNominationAppointmentForm = new ConfirmNominationAppointmentForm();
    private GeneralCaseNoteForm generalCaseNoteForm = new GeneralCaseNoteForm();

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

    public CaseProcessingFormDto build() {
      return new CaseProcessingFormDto(nominationQaChecksForm, nominationDecisionForm, withdrawNominationForm,
          confirmNominationAppointmentForm, generalCaseNoteForm);
    }
  }

}
