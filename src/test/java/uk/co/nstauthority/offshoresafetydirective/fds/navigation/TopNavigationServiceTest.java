package uk.co.nstauthority.offshoresafetydirective.fds.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class TopNavigationServiceTest {

  @InjectMocks
  private TopNavigationService topNavigationService;

  @Test
  void getTopNavigationItems_verifyAllTopNavigationItems() {
    var topNavigationItems = topNavigationService.getTopNavigationItems();

    assertThat(topNavigationItems).hasSize(1);
    assertThat(topNavigationItems.get(0))
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            WorkAreaController.WORK_AREA_TITLE,
            StringUtils.stripEnd(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), "/")
        );
  }
}