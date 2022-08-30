package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.ReviewAndSubmitTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class ReviewAndSubmitTaskListItem implements NominationTaskListItem {

  static final int ITEM_DISPLAY_ORDER = 10;

  @Override
  public String getItemDisplayText() {
    return ReviewAndSubmitTaskListSection.SECTION_NAME;
  }

  @Override
  public String getActionUrl(NominationTaskListItemType target) {
    return ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(target.nominationId()));
  }

  @Override
  public int getDisplayOrder() {
    return ITEM_DISPLAY_ORDER;
  }

  @Override
  public boolean isValid(NominationTaskListItemType target) {
    return true;
  }

  @Override
  public Class<? extends TaskListSection<NominationTaskListItemType>> getTaskListSection() {
    return ReviewAndSubmitTaskListSection.class;
  }

  @Override
  public boolean showTaskListLabels(NominationTaskListItemType target) {
    return false;
  }
}
