package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@ExtendWith(MockitoExtension.class)
class NominationWorkAreaItemServiceTest {

  @Mock
  private NominationWorkAreaQueryService nominationWorkAreaQueryService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private NominationWorkAreaItemTransformerService nominationWorkAreaItemTransformerService;

  private NominationWorkAreaItemService nominationWorkAreaItemService;

  @BeforeEach
  void setUp() {
    this.nominationWorkAreaItemService = new NominationWorkAreaItemService(nominationWorkAreaItemTransformerService);
  }

  @Test
  void getWorkAreaItems_whenNoResults_thenEmpty() {
    when(nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos()).thenReturn(List.of());
    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();
    assertThat(result).isEmpty();
  }

  @Test
  void getWorkAreaItems_whenItemReturned_thenHasCorrectPropertiesSet() {
    var baseTime = Instant.now();

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withCreatedTime(baseTime.minus(Period.ofDays(5)))
        .withSubmittedTime(baseTime)
        .withApplicantOrganisationId(1)
        .withNominatedOrganisationId(2)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var applicantOrganisation = new PortalOrganisationDto("1", "App org");
    var nominatedOrganisation = new PortalOrganisationDto("2", "Nominated org");
    when(portalOrganisationUnitQueryService.getOrganisationById(1)).thenReturn(Optional.of(applicantOrganisation));
    when(portalOrganisationUnitQueryService.getOrganisationById(2)).thenReturn(Optional.of(nominatedOrganisation));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result).hasSize(1);

    assertThat(result.get(0).modelProperties().getProperties().entrySet())
        .containsExactlyInAnyOrder(
            Map.entry("status",
                NominationStatus.SUBMITTED.getScreenDisplayText()),
            Map.entry("nominationType",
                queryResult.getNominationDisplayType().getDisplayText()),
            Map.entry("applicantOrganisation",
                applicantOrganisation.name()),
            Map.entry("nominationOrganisation",
                nominatedOrganisation.name()),
            Map.entry("applicantReference",
                queryResult.getApplicantReference().reference())
        );
  }

  @Test
  void getWorkAreaItems_whenItemReturned_andMissingValues_thenHasDefaultText() {
    var baseTime = Instant.now();

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withCreatedTime(baseTime.minus(Period.ofDays(5)))
        .withSubmittedTime(baseTime)
        .withApplicantOrganisationId(1)
        .withNominatedOrganisationId(2)
        .withApplicantReference(null)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    when(portalOrganisationUnitQueryService.getOrganisationById(1)).thenReturn(Optional.empty());
    when(portalOrganisationUnitQueryService.getOrganisationById(2)).thenReturn(Optional.empty());

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result).hasSize(1);

    assertThat(result.get(0).modelProperties().getProperties().entrySet())
        .containsExactlyInAnyOrder(
            Map.entry("status",
                NominationStatus.SUBMITTED.getScreenDisplayText()),
            Map.entry("nominationType",
                queryResult.getNominationDisplayType().getDisplayText()),
            Map.entry("applicantOrganisation", "Not provided"),
            Map.entry("nominationOrganisation", "Not provided"),
            Map.entry("applicantReference", "Not provided")
        );
  }

  @Test
  void getWorkAreaItems_whenTwoDraftNominations_thenSortedByCreationDateWithLatestFirst() {

    var baseTime = Instant.now();

    var earliestDraftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DRAFT)
        .withCreatedTime(baseTime.minus(Period.ofDays(5)))
        .build();

    var latestDraftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DRAFT)
        .withCreatedTime(baseTime.minus(Period.ofDays(2)))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(
        List.of(earliestDraftNomination, latestDraftNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result).map(WorkAreaItem::actionUrl)
        .containsExactly(
            ReverseRouter.route(
                on(NominationTaskListController.class).getTaskList(latestDraftNomination.getNominationId())),
            ReverseRouter.route(
                on(NominationTaskListController.class).getTaskList(earliestDraftNomination.getNominationId()))
        );
  }

  @Test
  void getWorkAreaItems_whenTwoSubmittedNominations_thenSortedBySubmittedDateWithLatestFirst() {

    var baseTime = Instant.now();

    var earliestNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withSubmittedTime(baseTime.minus(Period.ofDays(5)))
        .build();

    var latestNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withSubmittedTime(baseTime.minus(Period.ofDays(2)))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(
        List.of(earliestNomination, latestNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .map(WorkAreaItem::headingText)
        .containsExactly(
            latestNomination.getNominationReference().reference(),
            earliestNomination.getNominationReference().reference()
        );
  }

  @ParameterizedTest
  @EnumSource(NominationStatus.class)
  void getWorkAreaItems_verifyActionUrlForStatuses(NominationStatus status) throws Exception {

    var baseTime = Instant.now();

    var nomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(status)
        .withCreatedTime(baseTime)
        .withSubmittedTime(baseTime.minus(Period.ofDays(5)))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(nomination));

    if (status == NominationStatus.DELETED) {
      assertThatThrownBy(() -> nominationWorkAreaItemService.getNominationWorkAreaItems())
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessage("Nomination with ID [%d] should not appear in work area as status is [%s]"
              .formatted(
                  nomination.getNominationId().id(),
                  nomination.getNominationStatus().name()
              ));
      return;
    }

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    var assertion = assertThat(result)
        .hasSize(1)
        .map(WorkAreaItem::actionUrl)
        .first();

    switch (status) {
      case DRAFT -> assertion.isEqualTo(
          ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nomination.getNominationId())));
      case SUBMITTED -> assertion.isEqualTo(
          ReverseRouter.route(
              on(NominationCaseProcessingController.class).renderCaseProcessing(nomination.getNominationId())));
      default -> throw new Exception("Status [%s] case not covered".formatted(status));
    }

  }

  @Test
  void getWorkAreaItems_whenDraftAndSubmittedNominations_thenDraftsAppearFirst() {

    var baseTime = Instant.now();

    var draftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DRAFT)
        .withSubmittedTime(baseTime.minus(Period.ofDays(5)))
        .build();

    var submittedNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withSubmittedTime(baseTime.minus(Period.ofDays(2)))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(
        List.of(submittedNomination, draftNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result).map(WorkAreaItem::actionUrl)
        .containsExactly(
            ReverseRouter.route(
                on(NominationTaskListController.class).getTaskList(draftNomination.getNominationId())),
            ReverseRouter.route(
                on(NominationCaseProcessingController.class)
                    .renderCaseProcessing(submittedNomination.getNominationId()))
        );
  }

  @Test
  void getNominationWorkAreaItems_whenDraft_andFirstVersion_thenAssertHeadingAndCaption() {

    var createdDateTime = LocalDateTime.of(2022, 11, 23, 16, 40, 15);

    var draftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DRAFT)
        .withCreatedTime(createdDateTime.toInstant(ZoneOffset.UTC))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(draftNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .extracting(
            WorkAreaItem::headingText,
            WorkAreaItem::captionText
        ).containsExactly(
            Tuple.tuple("Draft nomination", "Created on 23 Nov 2022 16:40")
        );
  }

  @Test
  void getNominationWorkAreaItems_whenDraft_andNotFirstVersion_thenAssertHeadingAndCaption() {

    var createdDateTime = LocalDateTime.of(2022, 11, 23, 16, 40, 15);

    var draftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DRAFT)
        .withCreatedTime(createdDateTime.toInstant(ZoneOffset.UTC))
        .withNominationVersion(2)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(draftNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .extracting(
            WorkAreaItem::headingText,
            WorkAreaItem::captionText
        ).containsExactly(
            Tuple.tuple(draftNomination.getNominationReference().reference(), "Created on 23 Nov 2022 16:40")
        );
  }

  @Test
  void getNominationWorkAreaItems_whenSubmitted_thenAssertHeadingAndCaption() {

    var submittedTime = LocalDateTime.of(2022, 11, 23, 16, 40, 15);

    var submittedNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withSubmittedTime(submittedTime.toInstant(ZoneOffset.UTC))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(submittedNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .extracting(
            WorkAreaItem::headingText,
            WorkAreaItem::captionText
        ).containsExactly(
            Tuple.tuple(submittedNomination.getNominationReference().reference(), null)
        );

  }

  @Test
  void getNominationWorkAreaItems_whenDeleted_thenThrowsError() {

    var deletedNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DELETED)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(deletedNomination));

    assertThrows(IllegalStateException.class, () -> nominationWorkAreaItemService.getNominationWorkAreaItems());

  }
}