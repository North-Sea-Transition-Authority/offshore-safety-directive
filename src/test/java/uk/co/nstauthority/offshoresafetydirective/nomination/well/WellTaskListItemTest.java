package uk.co.nstauthority.offshoresafetydirective.nomination.well;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.OperatorshipTaskListSection;

@ExtendWith(MockitoExtension.class)
class WellTaskListItemTest {

  private static final NominationId NOMINATION_ID = new NominationId(100);

  @Mock
  private WellSubmissionService wellSubmissionService;

  @InjectMocks
  private WellTaskListItem wellTaskListItem;

  @Test
  void getItemDisplayText() {
    assertEquals(
        wellTaskListItem.getItemDisplayText(),
        WellTaskListItem.ITEM_DISPLAY_NAME
    );
  }

  @Test
  void getActionUrl() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    assertEquals(
        wellTaskListItem.getActionUrl(new NominationTaskListItemType(nominationDetail)),
        ReverseRouter.route(on(WellJourneyManagerController.class).wellJourneyManager(NOMINATION_ID))
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        wellTaskListItem.getDisplayOrder(),
        WellTaskListItem.ITEM_DISPLAY_ORDER
    );
  }

  @Test
  void getTaskListSection() {
    assertEquals(
        wellTaskListItem.getTaskListSection(),
        OperatorshipTaskListSection.class
    );
  }

  @Test
  void isValid_whenFormIsValid_thenTrue() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(wellSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    assertTrue(
        wellTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }

  @Test
  void isValid_whenFormIsInvalid_thenFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(wellSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    assertFalse(
        wellTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }

}