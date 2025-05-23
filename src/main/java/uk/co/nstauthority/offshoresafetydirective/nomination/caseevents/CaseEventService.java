package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;

@Service
public class CaseEventService {

  private final CaseEventRepository caseEventRepository;
  private final UserDetailService userDetailService;
  private final Clock clock;
  private final CaseEventFileService caseEventFileService;

  @Autowired
  public CaseEventService(CaseEventRepository caseEventRepository,
                          UserDetailService userDetailService,
                          Clock clock,
                          CaseEventFileService caseEventFileService) {
    this.caseEventRepository = caseEventRepository;
    this.userDetailService = userDetailService;
    this.clock = clock;
    this.caseEventFileService = caseEventFileService;
  }

  @Transactional
  public void createSubmissionEvent(NominationDetail nominationDetail) {
    createEvent(CaseEventType.NOMINATION_SUBMITTED, null, nominationDetail.getSubmittedInstant(), nominationDetail);
  }

  @Transactional
  public void createCompletedQaChecksEvent(NominationDetail nominationDetail, @Nullable String comment) {
    createEvent(CaseEventType.QA_CHECKS, comment, clock.instant(), nominationDetail);
  }

  @Transactional
  public void createDecisionEvent(NominationDetail nominationDetail, LocalDate decisionDate,
                                  String decisionComment,
                                  NominationDecision nominationDecision, List<UploadedFileForm> fileUploadForms) {
    var eventType = switch (nominationDecision) {
      case NO_OBJECTION -> CaseEventType.NO_OBJECTION_DECISION;
      case OBJECTION -> CaseEventType.OBJECTION_DECISION;
    };

    var caseEvent = createEvent(
        eventType,
        decisionComment,
        decisionDate.atStartOfDay().toInstant(ZoneOffset.UTC),
        nominationDetail
    );

    caseEventFileService.linkFilesToCaseEvent(caseEvent, fileUploadForms, FileDocumentType.DECISION);
  }

  @Transactional
  public void createWithdrawEvent(NominationDetail nominationDetail, String reason) {
    createEvent(CaseEventType.WITHDRAWN, reason, clock.instant(), nominationDetail);
  }

  @Transactional
  public void createAppointmentConfirmationEvent(NominationDetail nominationDetail,
                                                 LocalDate appointmentEffectiveDate,
                                                 @Nullable String comments,
                                                 List<UploadedFileForm> fileUploadForms) {
    var caseEvent = createEvent(CaseEventType.CONFIRM_APPOINTMENT, comments,
        appointmentEffectiveDate.atStartOfDay().toInstant(ZoneOffset.UTC), nominationDetail);
    if (!fileUploadForms.isEmpty()) {
      caseEventFileService.linkFilesToCaseEvent(caseEvent, fileUploadForms, FileDocumentType.APPOINTMENT_CONFIRMATION);
    }
  }

  @Transactional
  public void createGeneralCaseNoteEvent(NominationDetail nominationDetail, String subject, String body,
                                         List<UploadedFileForm> fileUploadForms) {
    var caseEvent = createEvent(CaseEventType.GENERAL_NOTE, subject, body, clock.instant(), nominationDetail);
    if (!fileUploadForms.isEmpty()) {
      caseEventFileService.linkFilesToCaseEvent(caseEvent, fileUploadForms, FileDocumentType.CASE_NOTE);
    }
  }

  @Transactional
  public void createSentForConsultationEvent(NominationDetail nominationDetail) {
    createEvent(CaseEventType.SENT_FOR_CONSULTATION, null, clock.instant(), nominationDetail);
  }

  @Transactional
  public void createConsultationResponseEvent(NominationDetail nominationDetail, String responseText,
                                              List<UploadedFileForm> fileUploadForms) {
    var caseEvent = createEvent(CaseEventType.CONSULTATION_RESPONSE, responseText, clock.instant(), nominationDetail);

    if (!fileUploadForms.isEmpty()) {
      caseEventFileService.linkFilesToCaseEvent(caseEvent, fileUploadForms, FileDocumentType.CONSULTATION_RESPONSE);
    }
  }

  @Transactional
  public void createUpdateRequestEvent(NominationDetail nominationDetail, String reason) {
    createEvent(CaseEventType.UPDATE_REQUESTED, reason, clock.instant(), nominationDetail);
  }

  private CaseEvent createEvent(CaseEventType caseEventType, String comment, Instant eventInstant,
                                NominationDetail nominationDetail) {
    return createEvent(caseEventType, null, comment, eventInstant, nominationDetail);
  }

  private CaseEvent createEvent(CaseEventType caseEventType, @Nullable String overrideTitle, String comment,
                                Instant eventInstant, NominationDetail nominationDetail) {
    var caseEvent = new CaseEvent();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    caseEvent.setCaseEventType(caseEventType);
    caseEvent.setTitle(overrideTitle);
    caseEvent.setComment(comment);
    caseEvent.setCreatedBy(userDetailService.getUserDetail().wuaId());
    caseEvent.setCreatedInstant(clock.instant());
    caseEvent.setEventInstant(eventInstant);
    caseEvent.setNomination(nominationDetail.getNomination());
    caseEvent.setNominationVersion(nominationDetailDto.version());
    return caseEventRepository.save(caseEvent);
  }

  // TODO OSDOP-344 - Replace with HQL query in NominationDetailCaseProcessingRepository
  public Optional<NominationDecision> getNominationDecisionForNominationDetail(NominationDetail nominationDetail) {
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var result = caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
        EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
        nominationDetail.getNomination(), nominationDetailDto.version());

    return result
        .map(CaseEvent::getCaseEventType)
        .map(caseEventType -> switch (caseEventType) {
          case OBJECTION_DECISION -> NominationDecision.OBJECTION;
          case NO_OBJECTION_DECISION -> NominationDecision.NO_OBJECTION;
          default -> null;
        });
  }
}
