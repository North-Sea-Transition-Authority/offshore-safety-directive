package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationDetailsTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class NomineeDetailTaskListItem implements NominationTaskListItem {

  static final int ITEM_DISPLAY_ORDER = 10;

  private final NomineeDetailSubmissionService nomineeDetailSubmissionService;

  @Autowired
  NomineeDetailTaskListItem(NomineeDetailSubmissionService nomineeDetailSubmissionService) {
    this.nomineeDetailSubmissionService = nomineeDetailSubmissionService;
  }

  @Override
  public String getItemDisplayText() {
    return NomineeDetailController.PAGE_NAME;
  }

  @Override
  public String getActionUrl(NominationTaskListItemType target) {
    return ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(target.nominationId()));
  }

  @Override
  public int getDisplayOrder() {
    return ITEM_DISPLAY_ORDER;
  }

  @Override
  public boolean isValid(NominationTaskListItemType target) {
    return nomineeDetailSubmissionService.isSectionSubmittable(target.nominationDetail());
  }

  @Override
  public Class<? extends TaskListSection<NominationTaskListItemType>> getTaskListSection() {
    return NominationDetailsTaskListSection.class;
  }
}
