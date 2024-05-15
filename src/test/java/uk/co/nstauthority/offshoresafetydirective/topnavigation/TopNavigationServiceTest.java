package uk.co.nstauthority.offshoresafetydirective.topnavigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordLandingPageController;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class TopNavigationServiceTest {

  @InjectMocks
  private TopNavigationService topNavigationService;

  @Test
  void getTopNavigationItems_verifyAllTopNavigationItems() {
    var topNavigationItems = topNavigationService.getTopNavigationItems();

    assertThat(topNavigationItems)
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            tuple(
                WorkAreaController.WORK_AREA_TITLE,
                StringUtils.stripEnd(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), "/")
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                StringUtils.stripEnd(
                    ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)),
                    "/"
                )
            ),
            tuple(
                TopNavigationService.SEARCH_SYSTEM_OF_RECORD_NAVIGATION_ITEM_TITLE,
                StringUtils.stripEnd(
                    ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()),
                    "/"
                )
            )
        );
  }
}