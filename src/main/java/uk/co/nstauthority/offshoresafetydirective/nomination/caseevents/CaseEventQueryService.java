package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;

@Service
public class CaseEventQueryService {

  private final CaseEventRepository caseEventRepository;
  private final CaseEventFileService caseEventFileService;
  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  CaseEventQueryService(CaseEventRepository caseEventRepository,
                        CaseEventFileService caseEventFileService,
                        EnergyPortalUserService energyPortalUserService) {
    this.caseEventRepository = caseEventRepository;
    this.caseEventFileService = caseEventFileService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public Optional<LocalDate> getDecisionDateForNominationDetail(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
            EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
            nominationDetail.getNomination(),
            dto.version())
        .map(caseEvent -> LocalDate.ofInstant(caseEvent.getEventInstant(), ZoneId.systemDefault()));
  }

  public Optional<LocalDate> getAppointmentConfirmationDateForNominationDetail(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
            EnumSet.of(CaseEventType.CONFIRM_APPOINTMENT),
            nominationDetail.getNomination(),
            dto.version())
        .map(caseEvent -> LocalDate.ofInstant(caseEvent.getEventInstant(), ZoneId.systemDefault()));
  }

  public List<CaseEventView> getCaseEventViewsForNominationDetail(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var events = caseEventRepository.findAllByNominationAndNominationVersion(
        nominationDetail.getNomination(),
        dto.version()
    );

    var createdByUserIds = events.stream()
        .map(caseEvent -> new WebUserAccountId(caseEvent.getCreatedBy()))
        .distinct()
        .toList();

    var users = energyPortalUserService.findByWuaIds(createdByUserIds);

    var userIdAndDtoMap = users.stream()
        .collect(Collectors.toMap(
            user -> (int) user.webUserAccountId(),
            user -> user
        ));

    var files = caseEventFileService.getFileViewMapFromCaseEvents(events);

    return events.stream()
        .map(caseEvent -> buildCaseEventView(userIdAndDtoMap, files.get(caseEvent)).apply(caseEvent))
        .sorted(Comparator.comparing(CaseEventView::getCreatedInstant, Comparator.reverseOrder()))
        .toList();
  }

  private Function<CaseEvent, CaseEventView> buildCaseEventView(Map<Integer, EnergyPortalUserDto> userIdAndNameMap,
                                                                List<CaseEventFileView> uploadedFileViews) {
    return caseEvent -> {
      var caseEventBuilder = CaseEventView.builder(
          Optional.ofNullable(caseEvent.getTitle()).orElse(caseEvent.getCaseEventType().getScreenDisplayText()),
          caseEvent.getNominationVersion(),
          caseEvent.getCreatedInstant(),
          caseEvent.getEventInstant(),
          userIdAndNameMap.get(caseEvent.getCreatedBy().intValue()).displayName()
      );

      return switch (caseEvent.getCaseEventType()) {
        case QA_CHECKS -> caseEventBuilder
            .withCustomDatePrompt("Completion date")
            .withCustomCreatorPrompt("Completed by")
            .withBody(caseEvent.getComment())
            .withCustomBodyPrompt("QA comments")
            .build();
        case NO_OBJECTION_DECISION, OBJECTION_DECISION -> caseEventBuilder
            .withCustomDatePrompt("Decision date")
            .withEventInstant(caseEvent.getEventInstant(), DateUtil.formatLongDate(caseEvent.getEventInstant()))
            .withCustomCreatorPrompt("Decided by")
            .withBody(caseEvent.getComment())
            .withCustomBodyPrompt("Decision comment")
            .withFileViews(uploadedFileViews)
            .withCustomFilePrompt("Decision document")
            .build();
        case WITHDRAWN -> caseEventBuilder
            .withCustomDatePrompt("Withdrawal date")
            .withCustomCreatorPrompt("Withdrawn by")
            .withBody(caseEvent.getComment())
            .withCustomBodyPrompt("Withdrawal reason")
            .build();
        case CONFIRM_APPOINTMENT -> caseEventBuilder
            .withCustomDatePrompt("Appointment date")
            .withEventInstant(caseEvent.getEventInstant(), DateUtil.formatLongDate(caseEvent.getEventInstant()))
            .withCustomCreatorPrompt("Confirmed by")
            .withBody(caseEvent.getComment())
            .withCustomBodyPrompt("Appointment comments")
            .withFileViews(uploadedFileViews)
            .withCustomFilePrompt("Appointment documents")
            .build();
        case GENERAL_NOTE -> caseEventBuilder
            .withBody(caseEvent.getComment())
            .withCustomBodyPrompt("Case note text")
            .withFileViews(uploadedFileViews)
            .withCustomFilePrompt("Case note documents")
            .build();
      };
    };
  }
}
