package uk.co.nstauthority.offshoresafetydirective.breadcrumb;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

public class NominationBreadcrumbUtil {

  private NominationBreadcrumbUtil() {
    throw new IllegalStateException("NominationBreadcrumbUtil is a util class and should not be instantiated");
  }

  public static BreadcrumbItem getNominationTaskListBreadcrumb(NominationId nominationId) {
    return new BreadcrumbItem(
        NominationTaskListController.PAGE_NAME,
        ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId))
    );
  }
}
