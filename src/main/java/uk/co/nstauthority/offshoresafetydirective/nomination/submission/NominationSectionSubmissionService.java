package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

/**
 * Interface to determine the behaviour of a nomination section during submission.
 */
public interface NominationSectionSubmissionService {

  boolean isSectionSubmittable(NominationDetail nominationDetail);

}