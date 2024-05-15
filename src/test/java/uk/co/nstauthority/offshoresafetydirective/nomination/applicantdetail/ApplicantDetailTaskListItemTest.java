package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.ApplicantTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailTaskListItemTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  @Mock
  private ApplicantDetailSubmissionService applicantDetailSubmissionService;

  @InjectMocks
  private ApplicantDetailTaskListItem applicantDetailTaskListItem;

  @Test
  void getItemDisplayText() {
    assertEquals(
        applicantDetailTaskListItem.getItemDisplayText(),
        ApplicantDetailController.PAGE_NAME
    );
  }

  @Test
  void getActionUrl() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    assertEquals(
        applicantDetailTaskListItem.getActionUrl(new NominationTaskListItemType(nominationDetail)),
        ReverseRouter.route(on(ApplicantDetailController.class).getUpdateApplicantDetails(NOMINATION_ID))
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        applicantDetailTaskListItem.getDisplayOrder(),
        ApplicantDetailTaskListItem.ITEM_DISPLAY_ORDER
    );
  }

  @Test
  void getTaskListSection() {
    assertEquals(
        applicantDetailTaskListItem.getTaskListSection(),
        ApplicantTaskListSection.class
    );
  }

  @Test
  void isValid_whenFormIsValid_thenTrue() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    assertTrue(
        applicantDetailTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }

  @Test
  void isValid_whenFormIsInvalid_thenFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    assertFalse(
        applicantDetailTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }

}