package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.stringutil.StringUtil;

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

  @ParameterizedTest
  @MethodSource("providePostSubmissionNominationStatuses")
  void getWorkAreaItems_whenPostSubmissionNomination_thenHasCorrectPropertiesSet(NominationStatus postSubmissionStatus) {
    var now = Instant.now();
    var pearsReference = "pears/ref/1";

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(postSubmissionStatus)
        .withCreatedTime(now.minus(Period.ofDays(5)))
        .withSubmittedTime(now)
        .withApplicantOrganisationId(1)
        .withNominatedOrganisationId(2)
        .withPearsReferences(pearsReference)
        .withHasNominationUpdateRequest(false)
        .withPlannedAppointmentDate(LocalDate.of(2025, 2, 26))
        .withFirstSubmittedOn(LocalDate.of(2024, 1, 10).atStartOfDay().toInstant(ZoneOffset.UTC))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var applicantOrganisation = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("Applicant org")
        .build();

    var nominatedOrganisation = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("Nominated org")
        .build();

    var ids = Stream.of(applicantOrganisation.id(), nominatedOrganisation.id())
        .map(PortalOrganisationUnitId::new)
        .toList();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(ids, NominationWorkAreaItemTransformerService.NOMINATED_OPERATORS_PURPOSE))
        .thenReturn(List.of(applicantOrganisation, nominatedOrganisation));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .extracting(workAreaItem -> workAreaItem.modelProperties().getProperties().entrySet())
        .containsExactlyInAnyOrder(
            Set.of(
              entry("status", postSubmissionStatus.getScreenDisplayText()),
              entry("nominationType", queryResult.getNominationDisplayType().getDisplayText()),
              entry("applicantOrganisation", applicantOrganisation.name()),
              entry("nominationOrganisation", nominatedOrganisation.name()),
              entry("applicantReference", queryResult.getApplicantReference().reference()),
              entry("pearsReferences", queryResult.getPearsReferences().references()),
              entry("hasUpdateRequest", false),
              entry("plannedAppointmentDate", "26 February 2025"),
              entry("nominationFirstSubmittedOn", "10 January 2024")
            )
        );
  }

  @ParameterizedTest
  @MethodSource("providePreSubmissionNominationStatuses")
  void getWorkAreaItems_whenPreSubmissionNomination_thenHasCorrectPropertiesSet(NominationStatus preSubmissionStatus) {
    var now = Instant.now();
    var pearsReference = "pears/ref/1";

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(preSubmissionStatus)
        // set the pears references and first submission date to prove they are not set on work area item even if provided
        .withPearsReferences(pearsReference)
        .withFirstSubmittedOn(LocalDate.of(2024, 1, 10).atStartOfDay().toInstant(ZoneOffset.UTC))
        // set other valid fields regardless of status
        .withCreatedTime(now.minus(Period.ofDays(5)))
        .withSubmittedTime(null)
        .withApplicantOrganisationId(1)
        .withNominatedOrganisationId(2)
        .withHasNominationUpdateRequest(false)
        .withPlannedAppointmentDate(LocalDate.of(2025, 2, 26))
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var applicantOrganisation = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("Applicant org")
        .build();

    var nominatedOrganisation = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("Nominated org")
        .build();

    var ids = Stream.of(applicantOrganisation.id(), nominatedOrganisation.id())
        .map(PortalOrganisationUnitId::new)
        .toList();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(ids, NominationWorkAreaItemTransformerService.NOMINATED_OPERATORS_PURPOSE))
        .thenReturn(List.of(applicantOrganisation, nominatedOrganisation));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .extracting(workAreaItem -> workAreaItem.modelProperties().getProperties().entrySet())
        .containsExactlyInAnyOrder(
            Set.of(
              entry("status", preSubmissionStatus.getScreenDisplayText()),
              entry("nominationType", queryResult.getNominationDisplayType().getDisplayText()),
              entry("applicantOrganisation", applicantOrganisation.name()),
              entry("nominationOrganisation", nominatedOrganisation.name()),
              entry("applicantReference", queryResult.getApplicantReference().reference()),
              entry("hasUpdateRequest", false),
              entry("plannedAppointmentDate", "26 February 2025")
            )
        );
  }

  @Test
  void getWorkAreaItems_whenItemReturned_andMissingValues_thenHasDefaultText() {

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withApplicantOrganisationId(null)
        .withNominatedOrganisationId(null)
        .withApplicantReference(null)
        .withPearsReferences(null)
        .withHasNominationUpdateRequest(false)
        .withPlannedAppointmentDate(null)
        .withFirstSubmittedOn(null)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .extracting(workAreaItem -> workAreaItem.modelProperties().getProperties().entrySet())
        .containsExactlyInAnyOrder(
            Set.of(
              entry("status", NominationStatus.SUBMITTED.getScreenDisplayText()),
              entry("nominationType", queryResult.getNominationDisplayType().getDisplayText()),
              entry("applicantOrganisation", "Not provided"),
              entry("nominationOrganisation", "Not provided"),
              entry("applicantReference", "Not provided"),
              entry("pearsReferences", ""),
              entry("hasUpdateRequest", false),
              entry("plannedAppointmentDate", "Not provided")
            )
        );
  }

  @Test
  void getWorkAreaItems_assertPearsReferenceAbbreviation() {
    var reference = "a".repeat(NominationWorkAreaItemService.PEARS_REFERENCE_MAX_LENGTH + 10);
    var expectedAbbreviation =
        "a".repeat(NominationWorkAreaItemService.PEARS_REFERENCE_MAX_LENGTH - 3) + StringUtil.ELLIPSIS_CHARACTER;
    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withPearsReferences(reference)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result)
        .hasSize(1);

    assertThat(result.get(0).modelProperties().getProperties().entrySet())
        .contains(
            entry("pearsReferences", expectedAbbreviation),
            entry("pearsReferencesAbbreviated", true)
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
          .hasMessage("Nomination with ID [%s] should not appear in work area as status is [%s]"
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
      case DELETED -> throw new Exception("Status [%s] case not covered".formatted(status));
      default -> {
        switch (status.getSubmissionStage()) {
          case PRE_SUBMISSION -> assertion.isEqualTo(ReverseRouter.route(
              on(NominationTaskListController.class).getTaskList(nomination.getNominationId())));
          case POST_SUBMISSION -> assertion.isEqualTo(ReverseRouter.route(
              on(NominationCaseProcessingController.class).renderCaseProcessing(nomination.getNominationId(), null)));
        }
      }
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
                    .renderCaseProcessing(submittedNomination.getNominationId(), null))
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
            Tuple.tuple("Draft nomination", "Created on 23 November 2022 16:40")
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
            Tuple.tuple(draftNomination.getNominationReference().reference(), "Created on 23 November 2022 16:40")
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

  @Test
  void getNominationWorkAreaItems_whenHasUpdateRequest_thenVerifyMapped() {
    var hasUpdateRequest = true;
    var draftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withHasNominationUpdateRequest(hasUpdateRequest)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(draftNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).modelProperties().getProperties())
        .extractingByKey("hasUpdateRequest")
        .isEqualTo(hasUpdateRequest);
  }

  @Test
  void getNominationWorkAreaItems_whenDoesNotHasUpdateRequest_thenVerifyNotMapped() {
    var hasUpdateRequest = false;
    var draftNomination = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .withHasNominationUpdateRequest(hasUpdateRequest)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(draftNomination));

    var result = nominationWorkAreaItemService.getNominationWorkAreaItems();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).modelProperties().getProperties())
        .containsEntry("hasUpdateRequest", false);
  }

  private static Stream<Arguments> providePostSubmissionNominationStatuses() {
    return NominationStatus.getPostSubmissionStatuses()
        .stream()
        .map(Arguments::of);
  }

  private static Stream<Arguments> providePreSubmissionNominationStatuses() {
    return NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.PRE_SUBMISSION)
        .stream()
        .filter(status -> !NominationStatus.DELETED.equals(status))
        .map(Arguments::of);
  }
}