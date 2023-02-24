package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

@ExtendWith(MockitoExtension.class)
class CaseEventQueryServiceTest {

  private static final int NOMINATION_DETAIL_VERSION = 2;

  @Mock
  private CaseEventRepository caseEventRepository;

  @Mock
  private CaseEventFileService caseEventFileService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private CaseEventQueryService caseEventQueryService;

  private CaseEventFileView caseEventFileView;
  private NominationDetail nominationDetail;
  private CaseEventTestUtil.Builder caseEventBuilder;
  private EnergyPortalUserDto caseEventCreator;

  @BeforeEach
  void setUp() {
    var uploadedFileView = new UploadedFileView("fileid", "filename", "filesize", "fileDescription", Instant.now());
    caseEventFileView = new CaseEventFileView(uploadedFileView, "/");

    nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(NOMINATION_DETAIL_VERSION)
        .build();

    var caseEventCreatorId = 100L;
    caseEventBuilder = CaseEventTestUtil.builder()
        .withNomination(nominationDetail.getNomination())
        .withNominationVersion(NOMINATION_DETAIL_VERSION)
        .withCreatedBy(caseEventCreatorId);

    caseEventCreator = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(caseEventCreatorId)
        .build();

  }

  @Test
  void getDecisionDateForNominationDetail_whenCaseEventFound_thenAssertDate() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var createdInstant = Instant.now();

    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .withNominationVersion(nominationVersion)
        .withCreatedInstant(createdInstant)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getDecisionDateForNominationDetail(detail);

    assertThat(result).contains(LocalDate.ofInstant(createdInstant, ZoneId.systemDefault()));
  }

  @Test
  void getDecisionDateForNominationDetail_whenCaseEventNotFound_thenEmptyOptional() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getDecisionDateForNominationDetail(detail);

    assertThat(result).isEmpty();
  }

  @Test
  void getAppointmentConfirmationDateForNominationDetail_whenCaseEventFound_thenAssertDate() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.CLOSED)
        .build();

    var confirmationInstant = Instant.now();

    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.CONFIRM_APPOINTMENT)
        .withNominationVersion(nominationVersion)
        .withEventInstant(confirmationInstant)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.CONFIRM_APPOINTMENT),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(detail);

    assertThat(result).contains(LocalDate.ofInstant(confirmationInstant, ZoneId.systemDefault()));
  }

  @Test
  void getAppointmentConfirmationDateForNominationDetail_whenCaseEventNotFound_thenEmptyOptional() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.CLOSED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.CONFIRM_APPOINTMENT),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getAppointmentConfirmationDateForNominationDetail(detail);

    assertThat(result).isEmpty();
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenQaChecksEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.QA_CHECKS;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var fields = List.of(
        "title",
        "eventInstant",
        "createdInstant",
        "createdBy",
        "nominationVersion",
        "body",
        "fileViews"
    );

    var prompts = List.of(
        "customVersionPrompt",
        "customDatePrompt",
        "customCreatorPrompt",
        "customBodyPrompt",
        "customFilePrompt"
    );

    var fieldsToNotAssert = List.of(
        "formattedEventTime"
    );

    var fieldsAndPrompts = Stream.concat(fields.stream(), prompts.stream()).toList();
    var allProperties = Stream.concat(fieldsAndPrompts.stream(), fieldsToNotAssert.stream()).toList();

    assertThat(result)
        .hasSize(1)
        .first()
        .hasOnlyFields(allProperties.toArray(String[]::new));

    assertThat(result.get(0))
        .extracting(fields.toArray(String[]::new))
        .containsExactly(
            caseEventType.getScreenDisplayText(),
            caseEvent.getEventInstant(),
            caseEvent.getCreatedInstant(),
            caseEventCreator.displayName(),
            NOMINATION_DETAIL_VERSION,
            caseEvent.getComment(),
            null
        );

    assertThat(result.get(0))
        .extracting(prompts.toArray(String[]::new))
        .containsExactly(
            null,
            "Completion date",
            "Completed by",
            "QA comments",
            null
        );
  }

  @ParameterizedTest
  @EnumSource(value = CaseEventType.class, names = {"NO_OBJECTION_DECISION", "OBJECTION_DECISION"})
  void getCaseEventViewsForNominationDetail_whenDecisionEvent_thenVerifyResult(CaseEventType caseEventType) {
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent)))
        .thenReturn(Map.of(caseEvent, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var fields = List.of(
        "title",
        "eventInstant",
        "createdInstant",
        "createdBy",
        "nominationVersion",
        "body",
        "fileViews",
        "formattedEventTime"
    );

    var prompts = List.of(
        "customVersionPrompt",
        "customDatePrompt",
        "customCreatorPrompt",
        "customBodyPrompt",
        "customFilePrompt"
    );

    var fieldsAndPrompts = Stream.concat(fields.stream(), prompts.stream()).toList();

    assertThat(result)
        .hasSize(1)
        .first()
        .hasOnlyFields(fieldsAndPrompts.toArray(String[]::new));

    assertThat(result.get(0))
        .extracting(fields.toArray(String[]::new))
        .containsExactly(
            caseEventType.getScreenDisplayText(),
            caseEvent.getEventInstant(),
            caseEvent.getCreatedInstant(),
            caseEventCreator.displayName(),
            NOMINATION_DETAIL_VERSION,
            caseEvent.getComment(),
            List.of(caseEventFileView),
            DateUtil.formatLongDate(caseEvent.getEventInstant())
        );

    assertThat(result.get(0))
        .extracting(prompts.toArray(String[]::new))
        .containsExactly(
            null,
            "Decision date",
            "Decided by",
            "Decision comment",
            "Decision document"
        );
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenWithdrawnEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.WITHDRAWN;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent)))
        .thenReturn(Map.of(caseEvent, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var fields = List.of(
        "title",
        "eventInstant",
        "createdInstant",
        "createdBy",
        "nominationVersion",
        "body",
        "fileViews"
    );

    var prompts = List.of(
        "customVersionPrompt",
        "customDatePrompt",
        "customCreatorPrompt",
        "customBodyPrompt",
        "customFilePrompt"
    );

    var fieldsToNotAssert = List.of(
        "formattedEventTime"
    );

    var fieldsAndPrompts = Stream.concat(fields.stream(), prompts.stream()).toList();
    var allProperties = Stream.concat(fieldsAndPrompts.stream(), fieldsToNotAssert.stream()).toList();

    assertThat(result)
        .hasSize(1)
        .first()
        .hasOnlyFields(allProperties.toArray(String[]::new));

    assertThat(result.get(0))
        .extracting(fields.toArray(String[]::new))
        .containsExactly(
            caseEventType.getScreenDisplayText(),
            caseEvent.getEventInstant(),
            caseEvent.getCreatedInstant(),
            caseEventCreator.displayName(),
            NOMINATION_DETAIL_VERSION,
            caseEvent.getComment(),
            null
        );

    assertThat(result.get(0))
        .extracting(prompts.toArray(String[]::new))
        .containsExactly(
            null,
            "Withdrawal date",
            "Withdrawn by",
            "Withdrawal reason",
            null
        );
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenConfirmAppointmentEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.CONFIRM_APPOINTMENT;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent)))
        .thenReturn(Map.of(caseEvent, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var fields = List.of(
        "title",
        "eventInstant",
        "createdInstant",
        "createdBy",
        "nominationVersion",
        "body",
        "fileViews",
        "formattedEventTime"
    );

    var prompts = List.of(
        "customVersionPrompt",
        "customDatePrompt",
        "customCreatorPrompt",
        "customBodyPrompt",
        "customFilePrompt"
    );

    var fieldsAndPrompts = Stream.concat(fields.stream(), prompts.stream()).toList();

    assertThat(result)
        .hasSize(1)
        .first()
        .hasOnlyFields(fieldsAndPrompts.toArray(String[]::new));

    assertThat(result.get(0))
        .extracting(fields.toArray(String[]::new))
        .containsExactly(
            caseEventType.getScreenDisplayText(),
            caseEvent.getEventInstant(),
            caseEvent.getCreatedInstant(),
            caseEventCreator.displayName(),
            NOMINATION_DETAIL_VERSION,
            caseEvent.getComment(),
            List.of(caseEventFileView),
            DateUtil.formatLongDate(caseEvent.getEventInstant())
        );

    assertThat(result.get(0))
        .extracting(prompts.toArray(String[]::new))
        .containsExactly(
            null,
            "Appointment date",
            "Confirmed by",
            "Appointment comments",
            "Appointment documents"
        );
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenGeneralNoteEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.GENERAL_NOTE;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent)))
        .thenReturn(Map.of(caseEvent, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var fields = List.of(
        "title",
        "eventInstant",
        "createdInstant",
        "createdBy",
        "nominationVersion",
        "body",
        "fileViews"
    );

    var prompts = List.of(
        "customVersionPrompt",
        "customDatePrompt",
        "customCreatorPrompt",
        "customBodyPrompt",
        "customFilePrompt"
    );

    var fieldsToNotAssert = List.of(
        "formattedEventTime"
    );

    var fieldsAndPrompts = Stream.concat(fields.stream(), prompts.stream()).toList();
    var allProperties = Stream.concat(fieldsAndPrompts.stream(), fieldsToNotAssert.stream()).toList();

    assertThat(result)
        .hasSize(1)
        .first()
        .hasOnlyFields(allProperties.toArray(String[]::new));

    assertThat(result.get(0))
        .extracting(fields.toArray(String[]::new))
        .containsExactly(
            caseEventType.getScreenDisplayText(),
            caseEvent.getEventInstant(),
            caseEvent.getCreatedInstant(),
            caseEventCreator.displayName(),
            NOMINATION_DETAIL_VERSION,
            caseEvent.getComment(),
            List.of(caseEventFileView)
        );

    assertThat(result.get(0))
        .extracting(prompts.toArray(String[]::new))
        .containsExactly(
            null,
            null,
            null,
            "Case note text",
            "Case note documents"
        );
  }

  @Test
  void getCaseEventViewsForNominationDetail_assertSort() {
    var caseEventType = CaseEventType.GENERAL_NOTE;
    var firstCaseEventByCreatedDate = caseEventBuilder
        .withCreatedInstant(Instant.now().plus(Period.ofDays(1)))
        .withCaseEventType(caseEventType)
        .build();

    var secondCaseEventByCreatedDate = caseEventBuilder
        .withCreatedInstant(Instant.now())
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(secondCaseEventByCreatedDate, firstCaseEventByCreatedDate));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(secondCaseEventByCreatedDate, firstCaseEventByCreatedDate)))
        .thenReturn(Map.of(firstCaseEventByCreatedDate, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    assertThat(result)
        .extracting(CaseEventView::getEventInstant)
        .containsExactly(
            firstCaseEventByCreatedDate.getEventInstant(),
            secondCaseEventByCreatedDate.getEventInstant()
        );
  }
}