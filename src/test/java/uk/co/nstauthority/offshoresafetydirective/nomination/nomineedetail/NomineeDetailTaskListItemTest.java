package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NomineeTaskListSection;

@ExtendWith(MockitoExtension.class)
class NomineeDetailTaskListItemTest {

  private static final NominationId NOMINATION_ID = new NominationId(100);

  @Mock
  private NomineeDetailSubmissionService nomineeDetailSubmissionService;

  @InjectMocks
  private NomineeDetailTaskListItem nomineeDetailTaskListItem;

  @Test
  void getItemDisplayText() {
    assertEquals(
        nomineeDetailTaskListItem.getItemDisplayText(),
        NomineeDetailController.PAGE_NAME
    );
  }

  @Test
  void getActionUrl() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    assertEquals(
        nomineeDetailTaskListItem.getActionUrl(new NominationTaskListItemType(nominationDetail)),
        ReverseRouter.route(on(NomineeDetailController.class).getNomineeDetail(NOMINATION_ID))
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        nomineeDetailTaskListItem.getDisplayOrder(),
        NomineeDetailTaskListItem.ITEM_DISPLAY_ORDER
    );
  }

  @Test
  void getTaskListSection() {
    assertEquals(
        nomineeDetailTaskListItem.getTaskListSection(),
        NomineeTaskListSection.class
    );
  }

  @Test
  void isValid_whenFormIsValid_thenTrue() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    assertTrue(
        nomineeDetailTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }

  @Test
  void isValid_whenFormIsInvalid_thenFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    assertFalse(
        nomineeDetailTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }
}
