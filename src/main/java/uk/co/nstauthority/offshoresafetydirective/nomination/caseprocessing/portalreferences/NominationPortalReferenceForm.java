package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import uk.co.fivium.formlibrary.input.StringInput;

public abstract class NominationPortalReferenceForm {

  private StringInput references = new StringInput("references", "references");

  public StringInput getReferences() {
    return references;
  }

  public NominationPortalReferenceForm setReferences(StringInput references) {
    this.references = references;
    return this;
  }
}
