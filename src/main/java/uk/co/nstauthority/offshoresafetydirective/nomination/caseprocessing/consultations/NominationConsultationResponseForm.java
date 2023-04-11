package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import uk.co.fivium.formlibrary.input.StringInput;

public class NominationConsultationResponseForm {

  private StringInput response = new StringInput("response", "response");

  public StringInput getResponse() {
    return response;
  }

  public void setResponse(StringInput response) {
    this.response = response;
  }
}
