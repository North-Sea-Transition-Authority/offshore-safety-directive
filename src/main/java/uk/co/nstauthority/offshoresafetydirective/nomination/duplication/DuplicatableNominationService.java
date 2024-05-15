package uk.co.nstauthority.offshoresafetydirective.nomination.duplication;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

public interface DuplicatableNominationService {

  void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail);

}
