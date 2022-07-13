package uk.co.nstauthority.offshoresafetydirective.breadcrumb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

class BreadcrumbsUtilTest {

  @Test
  void buildIntoModelAndView_whenBuilderMethodsApplied_assertCrumbListInRightOrder() {
    var modelAndView = new ModelAndView();
    var currentPage = "My current page";
    var breadcrumbPrompt = "Other page name";
    var breadcrumbEndpoint = "Other page URL";

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(currentPage)
        .addWorkAreaBreadcrumb()
        .addTaskListBreadcrumb()
        .addBreadcrumb(breadcrumbPrompt, breadcrumbEndpoint)
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    Map<String, String> expectedCrumbList = new LinkedHashMap<>();
    expectedCrumbList.put(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), "Work area");
    expectedCrumbList.put(ReverseRouter.route(on(NominationTaskListController.class).getTaskList()), "Task list");
    expectedCrumbList.put(breadcrumbEndpoint, breadcrumbPrompt);

    assertThat(modelAndView.getModelMap()).containsOnlyKeys(
        BreadcrumbsUtil.CURRENT_PAGE_MODEL_ATRR_NAME,
        BreadcrumbsUtil.MAP_MODEL_ATRR_NAME
    );

    assertEquals(currentPage, modelAndView.getModel().get(BreadcrumbsUtil.CURRENT_PAGE_MODEL_ATRR_NAME));
    assertEquals(expectedCrumbList, modelAndView.getModel().get(BreadcrumbsUtil.MAP_MODEL_ATRR_NAME));
  }

}