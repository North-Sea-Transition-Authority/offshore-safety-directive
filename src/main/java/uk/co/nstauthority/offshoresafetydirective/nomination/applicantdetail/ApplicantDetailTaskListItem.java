package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.ApplicantTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class ApplicantDetailTaskListItem implements NominationTaskListItem {

  static final int ITEM_DISPLAY_ORDER = 10;

  private final ApplicantDetailSubmissionService applicantDetailSubmissionService;

  @Autowired
  ApplicantDetailTaskListItem(ApplicantDetailSubmissionService applicantDetailSubmissionService) {
    this.applicantDetailSubmissionService = applicantDetailSubmissionService;
  }

  @Override
  public String getItemDisplayText() {
    return ApplicantDetailController.PAGE_NAME;
  }

  @Override
  public String getActionUrl(NominationTaskListItemType target) {
    return ReverseRouter.route(on(ApplicantDetailController.class).getUpdateApplicantDetails(target.nominationId()));
  }

  @Override
  public int getDisplayOrder() {
    return ITEM_DISPLAY_ORDER;
  }

  @Override
  public boolean isValid(NominationTaskListItemType target) {
    return applicantDetailSubmissionService.isSectionSubmittable(target.nominationDetail());
  }

  @Override
  public Class<? extends TaskListSection<NominationTaskListItemType>> getTaskListSection() {
    return ApplicantTaskListSection.class;
  }
}
