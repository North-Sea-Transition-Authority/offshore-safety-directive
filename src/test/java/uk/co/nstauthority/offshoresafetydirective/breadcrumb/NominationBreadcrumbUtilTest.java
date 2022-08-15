package uk.co.nstauthority.offshoresafetydirective.breadcrumb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@ExtendWith(MockitoExtension.class)
class NominationBreadcrumbUtilTest {

  @Test
  void getNominationTaskListBreadcrumb_assertExpectedBreadcrumbItem() {

    var nominationId = new NominationId(100);

    var resultingBreadcrumbItem = NominationBreadcrumbUtil.getNominationTaskListBreadcrumb(nominationId);

    assertThat(resultingBreadcrumbItem)
        .extracting(BreadcrumbItem::prompt, BreadcrumbItem::url)
        .containsExactly(
            NominationTaskListController.PAGE_NAME,
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId))
        );
  }
}