package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

/**
 * Interface to determine the behaviour of a nomination section during submission.
 */
public interface NominationSectionSubmissionService {

  /**
   * Method to determine if a nomination section is in a submittable state.
   * @param nominationDetail The nomination detail the section is associated to
   * @return true if the section can be submitted, false otherwise
   */
  boolean isSectionSubmittable(NominationDetail nominationDetail);

  /**
   * Method to call when a nomination detail is submitted. This can be used to run
   * section specific logic at submission time.
   * @param nominationDetail The nomination detail the section is being submitted for.
   */
  default void onSubmission(NominationDetail nominationDetail) {
  }
}