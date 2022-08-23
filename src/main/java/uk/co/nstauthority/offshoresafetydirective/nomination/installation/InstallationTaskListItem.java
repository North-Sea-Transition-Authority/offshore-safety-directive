package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.OperatorshipTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class InstallationTaskListItem implements NominationTaskListItem {

  static final int ITEM_DISPLAY_ORDER = 20;

  static final String ITEM_DISPLAY_NAME = "Installations";

  private final InstallationSubmissionService installationSubmissionService;

  @Autowired
  InstallationTaskListItem(InstallationSubmissionService installationSubmissionService) {
    this.installationSubmissionService = installationSubmissionService;
  }

  @Override
  public String getItemDisplayText() {
    return ITEM_DISPLAY_NAME;
  }

  @Override
  public String getActionUrl(NominationTaskListItemType target) {
    return ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(target.nominationId()));
  }

  @Override
  public int getDisplayOrder() {
    return ITEM_DISPLAY_ORDER;
  }

  @Override
  public boolean isValid(NominationTaskListItemType target) {
    return installationSubmissionService.isSectionSubmittable(target.nominationDetail());
  }

  @Override
  public Class<? extends TaskListSection<NominationTaskListItemType>> getTaskListSection() {
    return OperatorshipTaskListSection.class;
  }
}
