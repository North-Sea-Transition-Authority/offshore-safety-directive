package uk.co.nstauthority.offshoresafetydirective.breadcrumb;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

public class Breadcrumbs {

  private final Set<BreadcrumbItem> breadcrumbItems;
  private final String currentPageName;

  private Breadcrumbs(Set<BreadcrumbItem> breadcrumbItems, String currentPageName) {
    this.breadcrumbItems = breadcrumbItems;
    this.currentPageName = currentPageName;
  }

  Set<BreadcrumbItem> getBreadcrumbItems() {
    return breadcrumbItems;
  }

  String getCurrentPageName() {
    return currentPageName;
  }

  public static class BreadcrumbsBuilder {
    private final String currentPage;
    private final Set<BreadcrumbItem> breadcrumbs = new LinkedHashSet<>();

    public BreadcrumbsBuilder(String currentPage) {
      this.currentPage = currentPage;
    }

    public BreadcrumbsBuilder addWorkAreaBreadcrumb() {
      breadcrumbs.add(new BreadcrumbItem("Work area", ReverseRouter.route(on(WorkAreaController.class).getWorkArea())));
      return this;
    }

    public BreadcrumbsBuilder addTaskListBreadcrumb() {
      breadcrumbs.add(new BreadcrumbItem("Task list", ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));
      return this;
    }

    public BreadcrumbsBuilder addBreadcrumb(String prompt, String endpoint) {
      breadcrumbs.add(new BreadcrumbItem(prompt, endpoint));
      return this;
    }

    public Breadcrumbs build() {
      return new Breadcrumbs(this.breadcrumbs, this.currentPage);
    }
  }
}
