package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationDetailsTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class RelatedInformationTaskListItem implements NominationTaskListItem {

  static final int ITEM_DISPLAY_ORDER = 20;

  private final RelatedInformationSubmissionService relatedInformationFormService;

  @Autowired
  RelatedInformationTaskListItem(
      RelatedInformationSubmissionService relatedInformationFormService) {
    this.relatedInformationFormService = relatedInformationFormService;
  }

  @Override
  public String getItemDisplayText() {
    return RelatedInformationController.PAGE_NAME;
  }

  @Override
  public String getActionUrl(NominationTaskListItemType target) {
    return ReverseRouter.route(on(RelatedInformationController.class).renderRelatedInformation(target.nominationId()));
  }

  @Override
  public int getDisplayOrder() {
    return ITEM_DISPLAY_ORDER;
  }

  @Override
  public boolean isVisible(NominationTaskListItemType target) {
    return NominationTaskListItem.super.isVisible(target);
  }

  @Override
  public Class<? extends TaskListSection<NominationTaskListItemType>> getTaskListSection() {
    return NominationDetailsTaskListSection.class;
  }

  @Override
  public boolean isValid(NominationTaskListItemType target) {
    return relatedInformationFormService.isSectionSubmittable(target.nominationDetail());
  }
}
