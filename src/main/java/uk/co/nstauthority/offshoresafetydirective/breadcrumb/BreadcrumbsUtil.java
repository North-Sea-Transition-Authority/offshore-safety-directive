package uk.co.nstauthority.offshoresafetydirective.breadcrumb;

import java.util.LinkedHashMap;
import org.springframework.web.servlet.ModelAndView;

public class BreadcrumbsUtil {

  static final String MAP_MODEL_ATRR_NAME = "breadcrumbsList";
  static final String CURRENT_PAGE_MODEL_ATRR_NAME = "currentPage";


  private BreadcrumbsUtil() {
    throw new IllegalStateException("BreadcrumbsUtil is an util class and should not be initialized");
  }

  public static void addBreadcrumbsToModel(ModelAndView modelAndView, Breadcrumbs breadcrumbs) {
    var breadcrumbMap = new LinkedHashMap<String, String>();
    breadcrumbs.getBreadcrumbItems()
        .forEach(breadcrumbItem -> breadcrumbMap.put(breadcrumbItem.url(), breadcrumbItem.prompt()));

    modelAndView.addObject(MAP_MODEL_ATRR_NAME, breadcrumbMap);
    modelAndView.addObject(CURRENT_PAGE_MODEL_ATRR_NAME, breadcrumbs.getCurrentPageName());
  }
}
