package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.OperatorshipTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class WellTaskListItem implements NominationTaskListItem {

  static final int ITEM_DISPLAY_ORDER = 10;

  static final String ITEM_DISPLAY_NAME = "Wells";

  private final WellSubmissionService wellSubmissionService;

  @Autowired
  WellTaskListItem(WellSubmissionService wellSubmissionService) {
    this.wellSubmissionService = wellSubmissionService;
  }

  @Override
  public String getItemDisplayText() {
    return ITEM_DISPLAY_NAME;
  }

  @Override
  public String getActionUrl(NominationTaskListItemType target) {
    return ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(target.nominationId()));
  }

  @Override
  public int getDisplayOrder() {
    return ITEM_DISPLAY_ORDER;
  }

  @Override
  public boolean isValid(NominationTaskListItemType target) {
    return wellSubmissionService.isSectionSubmittable(target.nominationDetail());
  }

  @Override
  public Class<? extends TaskListSection<NominationTaskListItemType>> getTaskListSection() {
    return OperatorshipTaskListSection.class;
  }
}
