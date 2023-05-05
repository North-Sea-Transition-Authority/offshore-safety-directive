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
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

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

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Completion date")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Completed by")
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "QA comments")
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
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

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Decision date")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Decided by")
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Decision comment")
        .hasFieldOrPropertyWithValue("fileViews", List.of(caseEventFileView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Decision document")
        .hasFieldOrPropertyWithValue("formattedEventTime", DateUtil.formatLongDate(caseEvent.getEventInstant()))
        .hasAssertedAllProperties();
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

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Withdrawal date")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Withdrawn by")
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Withdrawal reason")
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
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

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Appointment date")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Confirmed by")
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Appointment comments")
        .hasFieldOrPropertyWithValue("fileViews", List.of(caseEventFileView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Appointment documents")
        .hasFieldOrPropertyWithValue("formattedEventTime", DateUtil.formatLongDate(caseEvent.getEventInstant()))
        .hasAssertedAllProperties();
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

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", null)
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Case note text")
        .hasFieldOrPropertyWithValue("fileViews", List.of(caseEventFileView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Case note documents")
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenNominationSubmittedEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.NOMINATION_SUBMITTED;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Submitted on")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Submitted by")
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", null)
        .hasFieldOrPropertyWithValue("customBodyPrompt", null)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenSentForConsultationEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.SENT_FOR_CONSULTATION;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Date requested")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", null)
        .hasFieldOrPropertyWithValue("customBodyPrompt", null)
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenConsultationResponseEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.CONSULTATION_RESPONSE;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .withComment("response")
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent)))
        .thenReturn(Map.of(caseEvent, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Response date")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", null)
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Consultation response")
        .hasFieldOrPropertyWithValue("fileViews", List.of(caseEventFileView))
        .hasFieldOrPropertyWithValue("customFilePrompt", "Consultation response documents")
        .hasAssertedAllPropertiesExcept("formattedEventTime");
  }

  @Test
  void getCaseEventViewsForNominationDetail_whenNominationUpdateRequestedEvent_thenVerifyResult() {
    var caseEventType = CaseEventType.UPDATE_REQUESTED;
    var caseEvent = caseEventBuilder
        .withCaseEventType(caseEventType)
        .withComment("response")
        .build();

    when(caseEventRepository.findAllByNominationAndNominationVersion(nominationDetail.getNomination(),
        NOMINATION_DETAIL_VERSION))
        .thenReturn(List.of(caseEvent));

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(caseEventCreator.webUserAccountId()))))
        .thenReturn(List.of(caseEventCreator));

    when(caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent)))
        .thenReturn(Map.of(caseEvent, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    var caseEventAssertObject = assertThat(result)
        .hasSize(1)
        .first();

    new PropertyObjectAssert(caseEventAssertObject)
        .hasFieldOrPropertyWithValue("title", caseEventType.getScreenDisplayText())
        .hasFieldOrPropertyWithValue("eventInstant", caseEvent.getEventInstant())
        .hasFieldOrPropertyWithValue("customDatePrompt", "Date requested")
        .hasFieldOrPropertyWithValue("createdInstant", caseEvent.getCreatedInstant())
        .hasFieldOrPropertyWithValue("createdBy", caseEventCreator.displayName())
        .hasFieldOrPropertyWithValue("customCreatorPrompt", "Requested by")
        .hasFieldOrPropertyWithValue("nominationVersion", NOMINATION_DETAIL_VERSION)
        .hasFieldOrPropertyWithValue("customVersionPrompt", null)
        .hasFieldOrPropertyWithValue("body", caseEvent.getComment())
        .hasFieldOrPropertyWithValue("customBodyPrompt", "Reason for update")
        .hasFieldOrPropertyWithValue("fileViews", null)
        .hasFieldOrPropertyWithValue("customFilePrompt", null)
        .hasAssertedAllPropertiesExcept("formattedEventTime");
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

    when(caseEventFileService.getFileViewMapFromCaseEvents(
        List.of(secondCaseEventByCreatedDate, firstCaseEventByCreatedDate)))
        .thenReturn(Map.of(firstCaseEventByCreatedDate, List.of(caseEventFileView)));

    var result = caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail);

    assertThat(result)
        .extracting(CaseEventView::getEventInstant)
        .containsExactly(
            firstCaseEventByCreatedDate.getEventInstant(),
            secondCaseEventByCreatedDate.getEventInstant()
        );
  }

  @Test
  void getCaseEventForNominationDetail_whenFound_thenAssert() {
    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();
    var caseEvent = CaseEventTestUtil.builder().build();

    when(caseEventRepository.findByUuidAndNominationAndNominationVersion(
        caseEvent.getUuid(),
        nominationDetail.getNomination(),
        nominationDetailVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getCaseEventForNominationDetail(
        new CaseEventId(caseEvent.getUuid()),
        nominationDetail
    );
    assertThat(result).contains(caseEvent);
  }

  @Test
  void getCaseEventForNominationDetail_whenNotFound_thenAssertEmpty() {
    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();
    var caseEvent = CaseEventTestUtil.builder().build();

    when(caseEventRepository.findByUuidAndNominationAndNominationVersion(
        caseEvent.getUuid(),
        nominationDetail.getNomination(),
        nominationDetailVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getCaseEventForNominationDetail(
        new CaseEventId(caseEvent.getUuid()),
        nominationDetail
    );
    assertThat(result).isEmpty();
  }

  @Test
  void getLatestReasonForUpdate_whenCaseEventFound_thenAssertUpdateReason() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var reason = "reason";
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .withNominationVersion(nominationVersion)
        .withComment(reason)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventQueryService.getLatestReasonForUpdate(detail);

    assertThat(result).contains(reason);
  }

  @Test
  void getLatestReasonForUpdate_whenCaseEventNotFound_thenEmptyOptional() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.UPDATE_REQUESTED),
        detail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.empty());

    var result = caseEventQueryService.getLatestReasonForUpdate(detail);

    assertThat(result).isEmpty();
  }
}