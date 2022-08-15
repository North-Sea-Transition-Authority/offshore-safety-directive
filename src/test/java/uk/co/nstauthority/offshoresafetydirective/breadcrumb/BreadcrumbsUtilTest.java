package uk.co.nstauthority.offshoresafetydirective.breadcrumb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class BreadcrumbsUtilTest {

  @Test
  void buildIntoModelAndView_whenBuilderMethodsApplied_assertCrumbListInRightOrder() {
    var modelAndView = new ModelAndView();
    var currentPage = "My current page";

    var secondBreadcrumbItem = new BreadcrumbItem("second page name", "second page url");
    var thirdBreadcrumbItem = new BreadcrumbItem("third page name", "third page url");

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(currentPage)
        .addWorkAreaBreadcrumb()
        .addBreadcrumb(secondBreadcrumbItem.prompt(), secondBreadcrumbItem.url())
        .addBreadcrumb(thirdBreadcrumbItem)
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    assertThat(modelAndView.getModelMap()).containsOnlyKeys(
        BreadcrumbsUtil.CURRENT_PAGE_MODEL_ATRR_NAME,
        BreadcrumbsUtil.MAP_MODEL_ATRR_NAME
    );

    assertEquals(currentPage, modelAndView.getModel().get(BreadcrumbsUtil.CURRENT_PAGE_MODEL_ATRR_NAME));

    @SuppressWarnings("unchecked")
    var resultingBreadcrumbs = (Map<String, String>) modelAndView.getModel().get(BreadcrumbsUtil.MAP_MODEL_ATRR_NAME);

    assertThat(resultingBreadcrumbs).containsExactly(
        entry(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), WorkAreaController.WORK_AREA_TITLE),
        entry(secondBreadcrumbItem.url(), secondBreadcrumbItem.prompt()),
        entry(thirdBreadcrumbItem.url(), thirdBreadcrumbItem.prompt())
    );
  }

}