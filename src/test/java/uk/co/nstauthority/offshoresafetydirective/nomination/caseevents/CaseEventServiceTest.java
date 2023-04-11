package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

@ExtendWith(MockitoExtension.class)
class CaseEventServiceTest {

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private Clock clock;

  @Mock
  private CaseEventRepository caseEventRepository;

  @Mock
  private CaseEventFileService caseEventFileService;

  @InjectMocks
  private CaseEventService caseEventService;

  @Test
  void createCompletedQaChecksEvent() {
    var nominationVersion = 5;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();
    var comment = "comment text";

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createCompletedQaChecksEvent(nominationDetail, comment);

    var captor = ArgumentCaptor.forClass(CaseEvent.class);

    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.QA_CHECKS,
            null,
            comment,
            serviceUser.wuaId(),
            createdInstant,
            createdInstant,
            nominationDetail.getNomination(),
            nominationVersion
        );

  }

  @Test
  void createSubmissionEvent() {
    var submittedInstant = Instant.now();
    var nominationVersion = 5;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .withSubmittedInstant(submittedInstant)
        .build();

    when(clock.instant()).thenReturn(submittedInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createSubmissionEvent(nominationDetail);

    var captor = ArgumentCaptor.forClass(CaseEvent.class);

    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.NOMINATION_SUBMITTED,
            null,
            null,
            serviceUser.wuaId(),
            submittedInstant,
            submittedInstant,
            nominationDetail.getNomination(),
            nominationVersion
        );

  }

  @ParameterizedTest
  @EnumSource(NominationDecision.class)
  void createDecisionEvent(NominationDecision nominationDecision) {
    var nominationVersion = 5;
    var decisionDate = LocalDate.now();
    var comment = "comment text";
    var fileUploadForm = new FileUploadForm();

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    when(caseEventRepository.save(any(CaseEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

    caseEventService.createDecisionEvent(nominationDetail, decisionDate, comment, nominationDecision,
        List.of(fileUploadForm));

    var captor = ArgumentCaptor.forClass(CaseEvent.class);

    verify(caseEventRepository).save(captor.capture());

    var expectedCaseEventType = switch (nominationDecision) {
      case NO_OBJECTION -> CaseEventType.NO_OBJECTION_DECISION;
      case OBJECTION -> CaseEventType.OBJECTION_DECISION;
    };

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            expectedCaseEventType,
            null,
            comment,
            serviceUser.wuaId(),
            createdInstant,
            decisionDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            nominationDetail.getNomination(),
            nominationVersion
        );

    verify(caseEventFileService).finalizeFileUpload(captor.getValue(), List.of(fileUploadForm));
  }

  @ParameterizedTest
  @EnumSource(CaseEventType.class)
  void getNominationDecisionForNominationDetail_testMapping(CaseEventType caseEventType) {

    var nominationVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(caseEventType)
        .withNomination(nominationDetail.getNomination())
        .withNominationVersion(nominationVersion)
        .build();

    when(caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        nominationDetail.getNomination(),
        nominationVersion
    )).thenReturn(Optional.of(caseEvent));

    var result = caseEventService.getNominationDecisionForNominationDetail(nominationDetail);

    switch (caseEventType) {
      case NO_OBJECTION_DECISION -> assertThat(result).contains(NominationDecision.NO_OBJECTION);
      case OBJECTION_DECISION -> assertThat(result).contains(NominationDecision.OBJECTION);
      default -> assertThat(result).isEmpty();
    }
  }

  @Test
  void createWithdrawEvent() {
    var nominationVersion = 2;
    var reason = "reason";
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createWithdrawEvent(detail, reason);

    var captor = ArgumentCaptor.forClass(CaseEvent.class);
    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.WITHDRAWN,
            null,
            reason,
            serviceUser.wuaId(),
            createdInstant,
            createdInstant,
            detail.getNomination(),
            nominationVersion
        );
  }

  @Test
  void createAppointmentConfirmationEvent() {
    var nominationVersion = 2;
    var date = LocalDate.now();
    var comment = "comment text";
    var fileUploadForm = new FileUploadForm();
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createAppointmentConfirmationEvent(detail, date, comment, List.of(fileUploadForm));

    var captor = ArgumentCaptor.forClass(CaseEvent.class);
    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.CONFIRM_APPOINTMENT,
            null,
            comment,
            serviceUser.wuaId(),
            createdInstant,
            date.atStartOfDay().toInstant(ZoneOffset.UTC),
            detail.getNomination(),
            nominationVersion
        );
  }

  @Test
  void createGeneralCaseNoteEvent() {
    var nominationVersion = 2;
    var subject = "Test case note";
    var caseNoteText = "Body text";
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createGeneralCaseNoteEvent(detail, subject, caseNoteText, List.of(fileUploadForm));

    var captor = ArgumentCaptor.forClass(CaseEvent.class);
    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.GENERAL_NOTE,
            subject,
            caseNoteText,
            serviceUser.wuaId(),
            createdInstant,
            createdInstant,
            detail.getNomination(),
            nominationVersion
        );
  }

  @Test
  void createSentForConsultationEvent() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    caseEventService.createSentForConsultationEvent(detail);

    var captor = ArgumentCaptor.forClass(CaseEvent.class);
    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.SENT_FOR_CONSULTATION,
            null,
            null,
            serviceUser.wuaId(),
            createdInstant,
            createdInstant,
            detail.getNomination(),
            nominationVersion
        );
  }

  @Test
  void createConsultationResponseEvent() {
    var nominationVersion = 2;
    var detail = NominationDetailTestUtil.builder()
        .withVersion(nominationVersion)
        .build();

    var createdInstant = Instant.now();
    when(clock.instant()).thenReturn(createdInstant);

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    var responseText = "response";
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(UUID.randomUUID());

    // Return same arg as passed in to call
    when(caseEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    caseEventService.createConsultationResponseEvent(detail, responseText, List.of(fileUploadForm));

    var captor = ArgumentCaptor.forClass(CaseEvent.class);
    verify(caseEventRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            CaseEvent::getCaseEventType,
            CaseEvent::getTitle,
            CaseEvent::getComment,
            CaseEvent::getCreatedBy,
            CaseEvent::getCreatedInstant,
            CaseEvent::getEventInstant,
            CaseEvent::getNomination,
            CaseEvent::getNominationVersion
        ).containsExactly(
            CaseEventType.CONSULTATION_RESPONSE,
            null,
            responseText,
            serviceUser.wuaId(),
            createdInstant,
            createdInstant,
            detail.getNomination(),
            nominationVersion
        );

    verify(caseEventFileService).finalizeFileUpload(captor.getValue(), List.of(fileUploadForm));
  }
}