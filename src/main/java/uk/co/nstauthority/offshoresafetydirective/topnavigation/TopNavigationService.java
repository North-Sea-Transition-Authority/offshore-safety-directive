package uk.co.nstauthority.offshoresafetydirective.topnavigation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController.WORK_AREA_TITLE;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamManagementController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Service
public class TopNavigationService {

  public static final String TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE = "Manage teams";

  public List<TopNavigationItem> getTopNavigationItems() {
    var navigationItems = new ArrayList<TopNavigationItem>();
    navigationItems.add(
        new TopNavigationItem(WORK_AREA_TITLE, ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
    );

    navigationItems.add(
        new TopNavigationItem(TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberListRedirect()))
    );

    return navigationItems;
  }
}
