package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.OperatorshipTaskListSection;

@ExtendWith(MockitoExtension.class)
class InstallationTaskListItemTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  @Mock
  private InstallationSubmissionService installationSubmissionService;

  @InjectMocks
  private InstallationTaskListItem installationTaskListItem;

  @Test
  void getItemDisplayText() {
    assertEquals(
        InstallationTaskListItem.ITEM_DISPLAY_NAME,
        installationTaskListItem.getItemDisplayText()
    );
  }

  @Test
  void getActionUrl() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    assertEquals(
        installationTaskListItem.getActionUrl(new NominationTaskListItemType(nominationDetail)),
        ReverseRouter.route(on(InstallationJourneyManagerController.class).installationJourneyManager(NOMINATION_ID))
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        InstallationTaskListItem.ITEM_DISPLAY_ORDER,
        installationTaskListItem.getDisplayOrder()
    );
  }

  @Test
  void getTaskListSection() {
    assertEquals(
        installationTaskListItem.getTaskListSection(),
        OperatorshipTaskListSection.class
    );
  }

  @Test
  void isValid_whenFormIsValid_thenTrue() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    assertTrue(
        installationTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }

  @Test
  void isValid_whenFormIsInvalid_thenFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(installationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    assertFalse(
        installationTaskListItem.isValid(new NominationTaskListItemType(nominationDetail))
    );
  }
}